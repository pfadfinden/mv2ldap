package de.pfadfinden.mv.database;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.api.ValidatingPoolableLdapConnectionFactory;

public final class LdapDatabase {

    private static LdapConnectionPool pool;

    private LdapDatabase() {}

    public static LdapConnection getConnection() {

        if (pool == null){
            LdapConnectionConfig config = new LdapConnectionConfig();
            config.setLdapHost( "localhost" );
            config.setLdapPort(10389);
            ValidatingPoolableLdapConnectionFactory factory = new ValidatingPoolableLdapConnectionFactory(config);
            pool = new LdapConnectionPool(factory);
        }
        try {
            return pool.getConnection();
        } catch (LdapException e) {
            e.printStackTrace();
            return null;
        }
    }
}
