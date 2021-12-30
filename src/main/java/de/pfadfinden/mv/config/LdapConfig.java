package de.pfadfinden.mv.config;

import org.apache.directory.ldap.client.api.DefaultLdapConnectionFactory;
import org.apache.directory.ldap.client.api.DefaultPoolableLdapConnectionFactory;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapConnectionPool;
import org.apache.directory.ldap.client.template.LdapConnectionTemplate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LdapConfig {

    @Bean
    @ConfigurationProperties("app.ldap.connection")
    public LdapConnectionConfig provideLdapConnectionConfig(){
        return new LdapConnectionConfig();
    }

    @Bean
    public LdapConnectionPool provideLdapConnectionPool(LdapConnectionConfig ldapConnectionConfig) {
        DefaultLdapConnectionFactory factory = new DefaultLdapConnectionFactory(ldapConnectionConfig);
        return new LdapConnectionPool(new DefaultPoolableLdapConnectionFactory(factory));
    }

    @Bean
    public LdapConnectionTemplate provideLdapConnectionTemplate(LdapConnectionPool ldapConnectionPool) {
        return new LdapConnectionTemplate(ldapConnectionPool);
    }

}
