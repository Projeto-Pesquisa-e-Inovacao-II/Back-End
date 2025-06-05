package com.gabriel.infra;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBanco {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory("./") // garante que ele busque na raiz do projeto
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    private static final String URL = dotenv.get("BD_URL");
    private static final String USER = dotenv.get("BD_USER");
    private static final String PASSWORD = dotenv.get("BD_PASSWORD");

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("❌ Erro ao conectar no banco: " + e.getMessage());
            return null;
        }
    }
}
