#import "../lib.typ" : *

= SQL database schema<sql-database-schema>

== Overview

The project requirements slightly change from `pure_html` and `js`, where the latter requires the tracks to support an individual custom order within the playlist to which they are associated -- this is achieved via a simple addition in the SQL tables schema.

In both scenarios, the schema is composed by four tables: `user`, `track`, `playlist` and `playlist_tracks`.

#figure(
  image("../img/uml/uml_bluetto.png"),
  caption: [UML diagram.],
)<uml-diagram>

== The tables

- `users` table
```sql
CREATE TABLE IF NOT EXISTS users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  address VARCHAR(255) NOT NULL
);
```
This table is straightforward and standard. 
- The `id` attribute is the primary key, ensuring each user is uniquely identified.  
- The `username` attribute has a unique constraint to prevent duplicate usernames.  
- All other attributes (`password_hash`, `first_name`, `last_name`, `address`) are required but do not have uniqueness constraints.  
- There is no composite primary key because each user must have a unique `id`, and uniqueness for the username is enforced separately.

- `items` table
```sql
CREATE TABLE IF NOT EXISTS items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    cover_image VARCHAR(255) NOT NULL,
    price DOUBLE DEFAULT 0,
    sold BOOLEAN DEFAULT FALSE,
    created_by INT NOT NULL,
    FOREIGN KEY (created_by)
        REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);
```
This table stores all items sold and not sold.  
- `id` is the primary key.  
- `created_by` is a foreign key referencing `users.id`, ensuring each item is strictly associated with a user.  
- Attributes `name`, `description`, `price`, and `sold` are standard.  
- `cover_image` contains the relative path to the image file. The images are physically stored on disk under `\opt\auction\uploads`

- `auctions` table
```sql
CREATE TABLE IF NOT EXISTS auctions (
      id INT AUTO_INCREMENT PRIMARY KEY,
      creator_id INT NOT NULL,
      starting_price DOUBLE NOT NULL,
      min_increment INT NOT NULL,
      expiration DATETIME NOT NULL,
      closed BOOLEAN DEFAULT FALSE,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (creator_id) REFERENCES users(id)
);
```
This table stores auctions created by users.  
- `id` is the primary key.  
- `creator_id` is a foreign key referencing `users.id`, ensuring each auction is associated with a user.  
- Attributes `starting_price`, `min_increment`, `expiration`, `closed`, and `created_at` are standard.  
- `created_at` defaults to the current timestamp when the auction is created.  
- `closed` indicates whether the auction has ended, defaulting to FALSE.  


- `auction_items` table
```sql
CREATE TABLE IF NOT EXISTS auction_items (
       auction_id INT,
       item_id INT,
       PRIMARY KEY (auction_id, item_id),
       FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
       FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);
```
This table represents the many-to-many relationship between auctions and items.  
- `auction_id` is a foreign key referencing `auctions.id` with ON DELETE CASCADE.  
- `item_id` is a foreign key referencing `items.id` with ON DELETE CASCADE.  
- The primary key is a composite of `(auction_id, item_id)` to ensure that each item appears only once in a given auction.

- `offers` table
```sql
CREATE TABLE IF NOT EXISTS offers (
        id INT AUTO_INCREMENT PRIMARY KEY,
        auction_id INT NOT NULL,
        user_id INT NOT NULL,
        offered_price DOUBLE NOT NULL,
        offer_time DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
        FOREIGN KEY (user_id) REFERENCES users(id)
);
```
This table stores bid records for auctions.  
- The `id` attribute is the primary key, uniquely identifying each offer.  
- The `auction_id` attribute is a foreign key referencing `auctions.id`, linking the offer to its auction and using ON DELETE CASCADE.  
- The `user_id` attribute is a foreign key referencing `users.id`, linking the offer to the bidding user.  
- The `offered_price` attribute stores the bid amount.  
- The `offer_time` attribute records the time the offer was made, defaulting to the current timestamp.
- 
- `results` table
```sql
CREATE TABLE results (
                         auction_id INT PRIMARY KEY,
                         winner_id INT NOT NULL,
                         final_price DOUBLE NOT NULL,
                         shipping_address VARCHAR(100) NOT NULL,
                         FOREIGN KEY (auction_id) REFERENCES auctions(id),
                         FOREIGN KEY (winner_id) REFERENCES users(id)
);
```
This table stores the results of auctions after closure.  
- The `auction_id` attribute is the primary key, linking the result to a specific auction.  
- The `winner_id` attribute is a foreign key referencing `users.id`, indicating the user who won the auction.  
- The `final_price` attribute stores the final winning bid amount.  
- The `shipping_address` attribute stores the shipping address for the auction item.