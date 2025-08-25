package it.polimi.auction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DBUtil {
    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Properties props = new Properties();
            try (InputStream input = DBUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (input == null) {
                    throw new RuntimeException("db.properties file not found in classpath");
                }
                props.load(input);
                url = props.getProperty("db.url");
                user = props.getProperty("db.user");
                password = props.getProperty("db.password");
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("Failed to initialize DBUtil", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
// legacy version 
// package it.polimi.auction;

// import java.sql.Connection;
// import java.sql.DriverManager;
// import java.sql.SQLException;

// public class DBUtil {
//     private static final String URL = "jdbc:mysql://localhost:3306/online_auctions?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
//     private static final String USER = "root";
//     private static final String PASSWORD = "5449";

//     static {
//         try {
//             Class.forName("com.mysql.cj.jdbc.Driver");
//         } catch (ClassNotFoundException e) {
//             throw new RuntimeException("Failed to load MySQL driver", e);
//         }
//     }

//     public static Connection getConnection() throws SQLException {
//         return DriverManager.getConnection(URL, USER, PASSWORD);
//     }
// }