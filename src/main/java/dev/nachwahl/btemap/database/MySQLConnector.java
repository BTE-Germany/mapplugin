package dev.nachwahl.btemap.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnector {
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    private Connection connection;

    public MySQLConnector(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        if (!isConnected()) {
            String ANSI_RESET = "\u001B[0m";
            try {
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
                String ANSI_GREEN = "\u001B[32m";
                System.out.println("[PolyMap]" + ANSI_GREEN + " MySQL connection ok!" + ANSI_RESET);
            } catch (SQLException e) {
                e.printStackTrace();
                String ANSI_RED = "\u001B[31m";
                System.out.println("[PolyMap]" + ANSI_RED + " MySQL connection error" + ANSI_RESET);
            }
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
                System.out.println("[PolyMap] MySQL connection closed");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public boolean isConnected() {
        try {
            return (connection != null && !connection.isClosed() && connection.isValid(1000));
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public Connection getConnection() {
        if (!isConnected())
            connect();
        return connection;
    }

}
