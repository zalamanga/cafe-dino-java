package com.dinocafe.web;

import java.util.List;
import javax.faces.bean.*;

@ManagedBean(name = "catalog")
@ApplicationScoped
public class CatalogBean {
  public static class Drink {
    private final String name, image;

    Drink(String name, String image) {
      this.name = name;
      this.image = image;
    }

    public String getName() {
      return name;
    }

    public String getImage() {
      return image;
    }
  }

  public List<Drink> getDrinks() {
    return List.of(
        new Drink("Brown Sugar Fresh Milk", "bsfm.jpg"),
            new Drink("Brown Sugar Milk Tea", "bsmt.jpg"),
        new Drink("Choco Hazelnut", "chocohazelnut.jpg"),
            new Drink("Choco Classic", "chococlassic.jpg"),
        new Drink("Mango Milk", "mangomilk.jpg"),
            new Drink("Strawberry Milk", "strawberrymilk.jpg"),
        new Drink("Cappuccino", "cappucino.jpg"),
            new Drink("Hazelnut Coffee", "hazelnutcoffee.jpg"));
  }
}
