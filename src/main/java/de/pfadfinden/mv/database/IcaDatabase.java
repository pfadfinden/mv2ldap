package de.pfadfinden.mv.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class IcaDatabase {
    private static final HikariConfig config = new HikariConfig("/databaseIca.properties");
    private static final HikariDataSource dataSource = new HikariDataSource(config);

    // Privater Konstruktur verhindert Instanziierung durch Client
    private IcaDatabase() {}

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        dataSource.close();
    }
}