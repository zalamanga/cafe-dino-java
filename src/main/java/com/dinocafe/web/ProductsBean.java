package com.dinocafe.web;

import com.dinocafe.Store;
import java.io.Serializable;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;
import pojo.Product;

@ManagedBean(name = "products")
@ViewScoped
public class ProductsBean implements Serializable {
  private List<Product> rows = new ArrayList<>();
  private Long id, pendingId;
  private long version, pendingVersion;
  private String name, category, query = "", pendingName;
  private Integer price, stock;
  private boolean loadError;

  @PostConstruct
  public void init() {
    query =
        FacesContext.getCurrentInstance()
            .getExternalContext()
            .getRequestParameterMap()
            .getOrDefault("q", "");
    reload();
  }

  public void reload() {
    try {
      rows = new Store().products(query);
      loadError = false;
    } catch (IllegalArgumentException e) {
      rows = new ArrayList<>();
      loadError = true;
      AuthBean.error(e.getMessage());
    } catch (RuntimeException e) {
      rows = new ArrayList<>();
      loadError = true;
      AuthBean.error("Daftar produk gagal dimuat. Periksa database lalu pilih Muat ulang.");
    }
  }

  public void save() {
    try {
      if (price == null || stock == null)
        throw new IllegalArgumentException("Harga dan stok wajib diisi dengan bilangan bulat.");
      boolean creating = id == null;
      new Store().save(id, version, name, category, price, stock);
      clear();
      reload();
      info(creating ? "Produk ditambahkan." : "Perubahan produk disimpan.");
    } catch (IllegalArgumentException e) {
      AuthBean.error(e.getMessage());
    } catch (RuntimeException e) {
      AuthBean.error("Produk belum tersimpan. Muat ulang daftar dan coba lagi.");
    }
  }

  public void edit(Long selectedId) {
    try {
      Product item = new Store().get(selectedId);
      if (item == null)
        throw new IllegalArgumentException("Produk sudah dihapus. Muat ulang daftar.");
      id = item.getId();
      version = item.getVersion();
      name = item.getName();
      category = item.getCategory();
      price = item.getPrice();
      stock = item.getStock();
      cancelDelete();
      info("Produk dipilih. Ubah isian lalu simpan perubahan.");
    } catch (IllegalArgumentException e) {
      AuthBean.error(e.getMessage());
      reload();
    } catch (RuntimeException e) {
      AuthBean.error("Produk belum dapat dibuka. Coba lagi setelah database tersedia.");
    }
  }

  public void askDelete(Long selectedId) {
    try {
      Product item = new Store().get(selectedId);
      if (item == null)
        throw new IllegalArgumentException("Produk sudah dihapus. Muat ulang daftar.");
      pendingId = item.getId();
      pendingVersion = item.getVersion();
      pendingName = item.getName();
    } catch (IllegalArgumentException e) {
      AuthBean.error(e.getMessage());
      reload();
    } catch (RuntimeException e) {
      AuthBean.error("Konfirmasi hapus belum dapat dimuat. Coba lagi.");
    }
  }

  public void delete() {
    if (pendingId == null) return;
    try {
      new Store().delete(pendingId, pendingVersion);
      if (pendingId.equals(id)) clear();
      cancelDelete();
      reload();
      info("Produk dihapus.");
    } catch (IllegalArgumentException e) {
      AuthBean.error(e.getMessage());
      cancelDelete();
      reload();
    } catch (RuntimeException e) {
      AuthBean.error("Produk belum terhapus. Periksa database lalu coba lagi.");
    }
  }

  public void clear() {
    id = null;
    name = null;
    category = null;
    price = null;
    stock = null;
  }

  public void cancelDelete() {
    pendingId = null;
    pendingName = null;
  }

  private void info(String value) {
    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(value));
  }

  public List<Product> getRows() {
    return rows;
  }

  public Long getId() {
    return id;
  }

  public boolean isEditing() {
    return id != null;
  }

  public boolean isLoadError() {
    return loadError;
  }

  public Long getPendingId() {
    return pendingId;
  }

  public String getPendingName() {
    return pendingName;
  }

  public String getName() {
    return name;
  }

  public void setName(String value) {
    name = value;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String value) {
    category = value;
  }

  public Integer getPrice() {
    return price;
  }

  public void setPrice(Integer value) {
    price = value;
  }

  public Integer getStock() {
    return stock;
  }

  public void setStock(Integer value) {
    stock = value;
  }

  public String getQuery() {
    return query;
  }
}
