
package com.cinematch.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {
        Properties properties = new Properties();

        try (InputStream input =
                     DatabaseConnection.class
                         .getClassLoader()
                         .getResourceAsStream("database.properties")) {

            if (input == null) {
                throw new SQLException(
                    "database.properties file not found."
                );
            }

            properties.load(input);

        } catch (IOException e) {
            throw new SQLException(
                "Could not read database configuration.", e
            );
        }

        return DriverManager.getConnection(
            properties.getProperty("db.url"),
            properties.getProperty("db.username"),
            properties.getProperty("db.password")
        );
    }
}
