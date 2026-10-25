package com.dinocafe;

import java.util.*;
import java.util.function.Function;
import org.hibernate.*;
import pojo.*;

public class Store {
  private final SessionFactory factory;

  public Store() {
    this(Database.factory());
  }

  public Store(SessionFactory factory) {
    this.factory = factory;
  }

  private <T> T transaction(Function<Session, T> work) {
    try (Session session = factory.openSession()) {
      Transaction transaction = session.beginTransaction();
      try {
        T result = work.apply(session);
        transaction.commit();
        return result;
      } catch (RuntimeException e) {
        if (transaction.isActive()) transaction.rollback();
        throw e;
      }
    }
  }

  public static String email(String value) {
    String normalized = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    if (normalized.length() > 254 || !normalized.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
      throw new IllegalArgumentException("Masukkan alamat email yang valid.");
    return normalized;
  }

  public Employees register(String name, String email, String password) {
    String cleanName = text(name, 80, "Nama harus diisi, maksimal 80 karakter.");
    String cleanEmail = email(email);
    if (password == null || password.length() < 12 || password.length() > 128)
      throw new IllegalArgumentException("Password harus 12 sampai 128 karakter.");
    String hash = Passwords.hash(password);
    return transaction(
        s -> {
          if (s.createQuery("from Employees where email=:email", Employees.class)
                  .setParameter("email", cleanEmail)
                  .uniqueResult()
              != null)
            throw new IllegalArgumentException(
                "Email sudah terdaftar. Gunakan email lain atau masuk.");
          Employees account = new Employees();
          account.setName(cleanName);
          account.setEmail(cleanEmail);
          account.setPasswordHash(hash);
          s.persist(account);
          return account;
        });
  }

  public Employees authenticate(String email, String password) {
    String cleanEmail = email(email);
    Employees account =
        transaction(
            s ->
                s.createQuery("from Employees where email=:email", Employees.class)
                    .setParameter("email", cleanEmail)
                    .uniqueResult());
    return account != null
            && password != null
            && password.length() <= 128
            && Passwords.verify(password, account.getPasswordHash())
        ? account
        : null;
  }

  public List<Product> products(String query) {
    String q = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
    if (q.length() > 120) throw new IllegalArgumentException("Pencarian maksimal 120 karakter.");
    return transaction(
        s ->
            s.createQuery(
                    "from Product where lower(name) like :term escape '!' or lower(category) like :term escape '!' order by id",
                    Product.class)
                .setParameter(
                    "term", "%" + q.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%")
                .getResultList());
  }

  public Product get(long id) {
    return transaction(s -> s.get(Product.class, id));
  }

  public void save(Long id, long version, String name, String category, int price, int stock) {
    String cleanName = text(name, 120, "Nama produk harus diisi, maksimal 120 karakter.");
    String cleanCategory = text(category, 60, "Kategori harus diisi, maksimal 60 karakter.");
    if (price < 0 || price > 1_000_000_000 || stock < 0 || stock > 1_000_000)
      throw new IllegalArgumentException(
          "Harga harus 0 sampai 1.000.000.000; stok 0 sampai 1.000.000.");
    transaction(
        s -> {
          Product item = id == null ? new Product() : s.get(Product.class, id);
          if (item == null || (id != null && item.getVersion() != version))
            throw new IllegalArgumentException("Produk berubah atau dihapus. Muat ulang daftar.");
          item.setName(cleanName);
          item.setCategory(cleanCategory);
          item.setPrice(price);
          item.setStock(stock);
          if (id == null) s.persist(item);
          return null;
        });
  }

  public void delete(long id, long version) {
    transaction(
        s -> {
          Product item = s.get(Product.class, id);
          if (item == null || item.getVersion() != version)
            throw new IllegalArgumentException("Produk berubah atau dihapus. Muat ulang daftar.");
          s.delete(item);
          return null;
        });
  }

  private static String text(String value, int max, String error) {
    String clean = value == null ? "" : value.strip();
    if (clean.isEmpty() || clean.length() > max) throw new IllegalArgumentException(error);
    return clean;
  }
}
