package de.pfadfinden.mv.database;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.ldap.client.api.DefaultPoolableLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Philipp on 10.02.2016.
 */
public class LdapDatabase {

    public LdapDatabase(){}

    private static LdapConnectionTemplate ldapConnectionTemplate;

    public static LdapConnectionTemplate getLdapConnectionTemplate() {
        if (ldapConnectionTemplate == null){
            ldapConnectionTemplate = new LdapConnectionTemplate(getLdapConnectionPool());
        }
        return ldapConnectionTemplate;
    }

    public static Dn getBaseDn(){
        Properties prop = new Properties();
        try {
            InputStream input = LdapDatabase.class.getResourceAsStream("/databaseLdap.properties");
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getLdapConnectionTemplate().newDn(prop.getProperty("ldapConnection.baseDn"));
    }

    private static LdapConnectionPool getLdapConnectionPool() {

        Properties prop = new Properties();
        try {
            InputStream input = LdapDatabase.class.getResourceAsStream("/databaseLdap.properties");
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LdapConnectionConfig config = new LdapConnectionConfig();
        config.setLdapHost(prop.getProperty("ldapConnection.host"));
        config.setLdapPort(Integer.valueOf(prop.getProperty("ldapConnection.port")));
        if(prop.getProperty("ldapConnection.name") != null) {
            config.setName(prop.getProperty("ldapConnection.name"));
            config.setCredentials(prop.getProperty("ldapConnection.credentials"));
        }

        GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        if(prop.getProperty("ldapPool.testOnBorrow") == "true") {
            poolConfig.testOnBorrow = true;
        }
        return new LdapConnectionPool(new DefaultPoolableLdapConnectionFactory(config), poolConfig);
    }

}
