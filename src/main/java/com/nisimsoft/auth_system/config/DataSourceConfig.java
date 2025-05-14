package com.nisimsoft.auth_system.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.nisimsoft.auth_system.datasource.TenantDataSourceProvider;
import com.nisimsoft.auth_system.datasource.TenantRoutingDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean(name = "defaultDataSource")
    DataSource defaultDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driver) {

        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driver);
        return ds;
    }

    @Primary
    @Bean
    DataSource dataSource(
            @Qualifier("defaultDataSource") DataSource defaultDataSource,
            TenantDataSourceProvider provider) {
        return new TenantRoutingDataSource(defaultDataSource, provider);
    }
}
