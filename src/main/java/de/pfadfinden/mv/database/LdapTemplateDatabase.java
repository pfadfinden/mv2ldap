package de.pfadfinden.mv.database;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.directory.ldap.client.api.DefaultPoolableLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;

/**
 * Created by Philipp on 10.02.2016.
 */
public class LdapTemplateDatabase {

    private static LdapConnectionTemplate ldapConnectionTemplate;

    public static LdapConnectionTemplate getLdapConnectionTemplate() {
        if (ldapConnectionTemplate == null){
            ldapConnectionTemplate = new LdapConnectionTemplate(getLdapConnectionPool());

        }
        return ldapConnectionTemplate;
    }

    private static LdapConnectionPool getLdapConnectionPool() {
        LdapConnectionConfig config = new LdapConnectionConfig();
        config.setLdapHost("localhost");
        config.setLdapPort(10389);

        GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        poolConfig.testOnBorrow = true;

        return new LdapConnectionPool(new DefaultPoolableLdapConnectionFactory(config), poolConfig);
    }

}
