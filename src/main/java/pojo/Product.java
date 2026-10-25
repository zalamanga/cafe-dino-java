package pojo;

import java.io.Serializable;
import javax.persistence.*;

/** Cafe product model adapted from the original DinoCafe source, author SEAN. */
@Entity
@Table(name = "product")
public class Product implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, length = 60)
  private String category;

  @Column(nullable = false)
  private int price;

  @Column(nullable = false)
  private int stock;

  @Version private long version;

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
  }

  public long getVersion() {
    return version;
  }
}
