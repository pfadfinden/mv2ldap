package de.pfadfinden.mv.database;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.directory.ldap.client.api.DefaultPoolableLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Philipp on 10.02.2016.
 */
public class LdapDatabase {

    private static LdapConnectionTemplate ldapConnectionTemplate;

    public static LdapConnectionTemplate getLdapConnectionTemplate() {
        if (ldapConnectionTemplate == null){
            ldapConnectionTemplate = new LdapConnectionTemplate(getLdapConnectionPool());
        }
        return ldapConnectionTemplate;
    }

    private static LdapConnectionPool getLdapConnectionPool() {

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("src/main/resources/databaseLdap.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        LdapConnectionConfig config = new LdapConnectionConfig();
        config.setLdapHost(prop.getProperty("ldapConnection.host"));
        config.setLdapPort(Integer.valueOf(prop.getProperty("ldapConnection.port")));

        GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        if(prop.getProperty("ldapPool.testOnBorrow") == "true") {
            poolConfig.testOnBorrow = true;
        }
        return new LdapConnectionPool(new DefaultPoolableLdapConnectionFactory(config), poolConfig);
    }

}
