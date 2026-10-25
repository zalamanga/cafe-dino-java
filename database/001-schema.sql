-- Skema hasil pemulihan dari model lama. Semua produk/harga/stok di bawah adalah contoh demo.
CREATE TABLE employees (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  email VARCHAR(254) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE TABLE product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  category VARCHAR(60) NOT NULL,
  price INT NOT NULL CHECK(price >= 0),
  stock INT NOT NULL CHECK(stock >= 0),
  version BIGINT NOT NULL DEFAULT 0
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
INSERT INTO product (name,category,price,stock) VALUES
('Brown Sugar Fresh Milk','Milk',18000,12),
('Brown Sugar Milk Tea','Tea',18000,12),
('Choco Hazelnut','Chocolate',20000,10),
('Choco Classic','Chocolate',18000,10),
('Mango Milk','Milk',18000,12),
('Strawberry Milk','Milk',18000,12),
('Cappuccino','Coffee',20000,10),
('Hazelnut Coffee','Coffee',22000,10);
