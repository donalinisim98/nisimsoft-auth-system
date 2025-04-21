package com.nisimsoft.auth_system.datasource;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
public class DataSourceConfig {

    private final TenantDataSourceProvider tenantDataSourceProvider;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dbDriver;

    // 🧩 Base de datos por defecto (donde está ns_corp)
    @Bean
    public DataSource defaultDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dbDriver);
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }

    // 🧠 DataSource dinámico principal basado en corpId
    @Primary
    @Bean
    public DataSource dataSource() {
        TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource(defaultDataSource(),
                tenantDataSourceProvider);

        Map<Object, Object> tenantSources = tenantDataSourceProvider.getTenantDataSources();
        tenantSources.forEach((key, ds) -> routingDataSource.addTenant((String) key, (DataSource) ds));

        return routingDataSource;
    }

    // 🧠 Configuración de JPA con el data source dinámico
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, EntityManagerFactoryBuilder builder) {

        return builder
                .dataSource(dataSource)
                .packages("com.nisimsoft.auth_system.entities") // <-- ajusta si tienes otro paquete
                .persistenceUnit("tenant-pu")
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
