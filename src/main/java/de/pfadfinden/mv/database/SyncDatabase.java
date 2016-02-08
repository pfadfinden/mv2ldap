package de.pfadfinden.mv.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class SyncDatabase {
    private static final HikariConfig config = new HikariConfig("src/main/resources/databaseSync.properties");
    private static final HikariDataSource dataSource = new HikariDataSource(config);

    // Privater Konstruktur verhindert Instanziierung durch Client
    private SyncDatabase() {}

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        dataSource.close();
    }
}