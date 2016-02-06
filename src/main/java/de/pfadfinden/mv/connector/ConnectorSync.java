package de.pfadfinden.mv.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectorSync {

    public static Connection getConnection() {
        Connection con = null;
        try {
            con = DriverManager.getConnection( "jdbc:mysql://localhost/bdp_ldapsync", "icauser", "Tester123#" );
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
