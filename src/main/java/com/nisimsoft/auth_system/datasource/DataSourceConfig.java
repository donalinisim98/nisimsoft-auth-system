package com.nisimsoft.auth_system.datasource;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    private final TenantDataSourceProvider tenantDataSourceProvider;

    @Bean(name = "defaultDataSource")
    @ConfigurationProperties("spring.datasource")
    public DataSource defaultDataSource(
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
    public DataSource dataSource(
            @Qualifier("defaultDataSource") DataSource defaultDataSource) {
        return new TenantRoutingDataSource(defaultDataSource, tenantDataSourceProvider);
    }
}
