-- Create the database
CREATE DATABASE online_auctions DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE online_auctions;

-- user table
CREATE TABLE IF NOT EXISTS users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  address VARCHAR(255) NOT NULL
);

-- article table
CREATE TABLE IF NOT EXISTS items (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(255),
  cover_image VARCHAR(255) NOT NULL,
  price INT DEFAULT 0,
  sold BOOLEAN DEFAULT FALSE,
  created_by INT NOT NULL,
  FOREIGN KEY (created_by) REFERENCES users(id)
			ON DELETE CASCADE
            ON UPDATE CASCADE
);

-- Auctions table
CREATE TABLE IF NOT EXISTS auctions (
      id INT AUTO_INCREMENT PRIMARY KEY,
      creator_id INT NOT NULL,
      starting_price INT NOT NULL,
      min_increment INT NOT NULL,
      expiration DATETIME NOT NULL,
      closed BOOLEAN DEFAULT FALSE,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (creator_id) REFERENCES users(id)
);

-- Auction-items relation (one auction can contain multiple items)
CREATE TABLE auction_items (
       auction_id INT,
       item_id INT,
       PRIMARY KEY (auction_id, item_id),
       FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
       FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- Offers table
CREATE TABLE offers (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        auction_id INT NOT NULL,
                        user_id INT NOT NULL,
                        offered_price DECIMAL(10,2) NOT NULL,
                        offer_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
                        FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Auction results (once an auction is closed and has a winner)
CREATE TABLE results (
                         auction_id INT PRIMARY KEY,
                         winner_id INT NOT NULL,
                         final_price INT NOT NULL,
                         shipping_address VARCHAR(100) NOT NULL,
                         FOREIGN KEY (auction_id) REFERENCES auctions(id),
                         FOREIGN KEY (winner_id) REFERENCES users(id)
);