package com.dinocafe;

import java.util.*;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import pojo.*;

class StoreTest {
  private SessionFactory factory;
  private Store store;

  @BeforeEach
  void isolatedDatabase() {
    Properties p = new Properties();
    p.setProperty(
        "hibernate.connection.url", "jdbc:h2:mem:cafe_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
    p.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
    p.setProperty("hibernate.connection.username", "sa");
    p.setProperty("hibernate.connection.password", "");
    p.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    p.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    factory = Database.build(p);
    store = new Store(factory);
  }

  @AfterEach
  void close() {
    if (factory != null) factory.close();
  }

  @Test
  void registrationAndLoginUseNormalizedEmailAndHash() {
    Employees account = store.register("Demo QA", " QA@example.test ", "Synthetic-password-2026");
    assertNotNull(account.getId());
    assertEquals("qa@example.test", account.getEmail());
    assertNotEquals("Synthetic-password-2026", account.getPasswordHash());
    assertEquals(
        account.getId(), store.authenticate("QA@EXAMPLE.TEST", "Synthetic-password-2026").getId());
    assertNull(store.authenticate("qa@example.test", "wrong"));
    assertNull(store.authenticate("absent@example.test", "wrong"));
    assertThrows(
        IllegalArgumentException.class,
        () -> store.register("Demo", "qa@example.test", "Synthetic-password-2026"));
  }

  @Test
  void invalidRegistrationDoesNotPersist() {
    assertThrows(
        IllegalArgumentException.class,
        () -> store.register("", "qa@example.test", "Synthetic-password-2026"));
    assertThrows(
        IllegalArgumentException.class,
        () -> store.register("Demo", "not-email", "Synthetic-password-2026"));
    assertThrows(
        IllegalArgumentException.class, () -> store.register("Demo", "qa@example.test", "short"));
    assertNull(store.authenticate("qa@example.test", "short"));
  }

  @Test
  void productRoundTripAndLiteralSearch() {
    store.save(null, 0, "100% Milk", "Milk", 18000, 12);
    Product item = store.products("100%").get(0);
    assertEquals(1, store.products("%").size());
    assertEquals(0, store.products("' or 1=1 --").size());
    assertEquals(1, store.products("milk").size());
    store.save(item.getId(), item.getVersion(), "Edited Milk", "Drink", 20000, 5);
    Product edited = store.get(item.getId());
    assertEquals("Edited Milk", edited.getName());
    assertEquals(5, edited.getStock());
    store.delete(edited.getId(), edited.getVersion());
    assertNull(store.get(item.getId()));
    assertTrue(store.products("").isEmpty());
  }

  @Test
  void invalidProductChangesAreRejectedWithoutDataLoss() {
    store.save(null, 0, "Demo Milk", "Milk", 18000, 12);
    Product item = store.products("").get(0);
    assertThrows(
        IllegalArgumentException.class,
        () -> store.save(item.getId(), item.getVersion(), "", "Milk", 18000, 12));
    assertThrows(
        IllegalArgumentException.class,
        () -> store.save(item.getId(), item.getVersion(), "Demo", "Milk", -1, 12));
    assertThrows(
        IllegalArgumentException.class,
        () -> store.save(item.getId(), item.getVersion(), "Demo", "Milk", 18000, -1));
    assertEquals("Demo Milk", store.get(item.getId()).getName());
  }

  @Test
  void staleUpdatesAndDeletesAreRejected() {
    store.save(null, 0, "Demo Milk", "Milk", 18000, 12);
    Product first = store.products("").get(0);
    store.save(first.getId(), first.getVersion(), "Newer Name", "Milk", 19000, 11);
    assertThrows(
        IllegalArgumentException.class,
        () -> store.save(first.getId(), first.getVersion(), "Stale", "Milk", 1, 1));
    assertThrows(
        IllegalArgumentException.class, () -> store.delete(first.getId(), first.getVersion()));
    assertEquals("Newer Name", store.get(first.getId()).getName());
  }
}
