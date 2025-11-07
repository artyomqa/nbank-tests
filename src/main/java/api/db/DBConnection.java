package api.db;

import common.configs.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String url = Config.getString("db.url");
    private static final String login = Config.getString("db.login");
    private static final String password = Config.getString("db.password");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, login, password);
    }
}
