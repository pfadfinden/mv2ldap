package de.pfadfinden.mv.database;

import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.api.ValidatingPoolableLdapConnectionFactory;

public final class LdapDatabase {

    private static LdapConnectionPool pool;

    private LdapDatabase() {}

    public static LdapConnectionPool getConnectionPool() {
        if (pool == null){
            LdapConnectionConfig config = new LdapConnectionConfig();
            config.setLdapHost( "localhost" );
            config.setLdapPort(10389);
            ValidatingPoolableLdapConnectionFactory factory = new ValidatingPoolableLdapConnectionFactory(config);
            pool = new LdapConnectionPool(factory);
        }
        return pool;
    }
}
