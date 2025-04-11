package com.gabriel.infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBanco {

    private static final String URL = System.getenv("BD_URL");
    private static final String USER = System.getenv("BD_USER");
    private static final String PASSWORD = System.getenv("BD_PASSWORD");
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
