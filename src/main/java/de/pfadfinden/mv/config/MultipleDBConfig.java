package de.pfadfinden.mv.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class MultipleDBConfig {

    // Primary: Configure Sync Database

    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.database-sync")
    public DataSourceProperties syncDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.database-synce")
    public DataSource syncDataSource() {
        return syncDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "jdbcSync")
    @Primary
    public JdbcTemplate syncJdbcTemplate() {
        return new JdbcTemplate(syncDataSource());
    }

    // Secondary: Configure Ica Database
    @Bean
    @ConfigurationProperties("app.datasource.database-ica")
    public DataSourceProperties icaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("app.datasource.database-ica")
    public DataSource icaDataSource() {
        return icaDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "jdbcIca")
    public JdbcTemplate icaJdbcTemplate() {
        return new JdbcTemplate(icaDataSource());
    }

}
