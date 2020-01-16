package de.pfadfinden.mv.config;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.directory.ldap.client.api.DefaultPoolableLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class LdapConfig {

    private final Logger logger = LoggerFactory.getLogger(LdapConfig.class);

    @Bean
    public LdapConnectionPool provideLdapConnectionPool() {

        Properties prop = new Properties();

        try {
            FileReader fr = new FileReader("./config/databaseLdap.properties");
            prop.load(fr);
        } catch (IOException e) {
            logger.error("Failed reading ldap database properties file.");
        }

        LdapConnectionConfig config = new LdapConnectionConfig();
        config.setLdapHost(prop.getProperty("ldapConnection.host"));
        config.setLdapPort(Integer.parseInt(prop.getProperty("ldapConnection.port")));
        if(prop.getProperty("ldapConnection.name") != null) {
            config.setName(prop.getProperty("ldapConnection.name"));
            config.setCredentials(prop.getProperty("ldapConnection.credentials"));
        }

        GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
        if(prop.getProperty("ldapPool.testOnBorrow").equals("true")) poolConfig.testOnBorrow = true;
        return new LdapConnectionPool(new DefaultPoolableLdapConnectionFactory(config), poolConfig);
    }

    @Bean
    public LdapConnectionTemplate provideLdapConnectionTemplate(LdapConnectionPool ldapConnectionPool) {
        return new LdapConnectionTemplate(ldapConnectionPool);
    }

}
