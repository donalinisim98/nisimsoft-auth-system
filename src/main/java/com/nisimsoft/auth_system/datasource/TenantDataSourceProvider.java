package com.nisimsoft.auth_system.datasource;

import com.nisimsoft.auth_system.entities.Corporation;
import com.nisimsoft.auth_system.entities.enums.NSCorpDBEngineEnum;
import com.nisimsoft.auth_system.repositories.CorporationRepository;
// import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Carga todos los DataSources de tenants definidos en la tabla ns_corp.
 */
@Component
@RequiredArgsConstructor
public class TenantDataSourceProvider {

    private final CorporationRepository corporationRepository;

    private final Map<Object, Object> tenantDataSources = new HashMap<>();

    // @PostConstruct
    public void loadAllTenantDataSources() {
        corporationRepository.findAll().forEach(this::createAndRegisterDataSource);
    }

    public Map<Object, Object> getTenantDataSources() {
        return tenantDataSources;
    }

    private void createAndRegisterDataSource(Corporation corp) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        String jdbcUrl = resolveJdbcUrl(corp);

        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(corp.getUsername());
        dataSource.setPassword(corp.getPassword());
        dataSource.setDriverClassName(resolveDriver(corp.getDbEngine()));

        // usa el id como identificador único
        tenantDataSources.put(String.valueOf(corp.getId()), dataSource);
    }

    private String resolveJdbcUrl(Corporation corp) {
        return switch (corp.getDbEngine()) {
            case MYSQL -> "jdbc:mysql://" + corp.getHost();
            case POSTGRESQL -> "jdbc:postgresql://" + corp.getHost();
            case MSSQL -> "jdbc:sqlserver://" + corp.getHost();
            case ORACLE -> "jdbc:oracle:thin:@" + corp.getHost();
        };
    }

    private String resolveDriver(NSCorpDBEngineEnum engine) {
        return switch (engine) {
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case POSTGRESQL -> "org.postgresql.Driver";
            case MSSQL -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case ORACLE -> "oracle.jdbc.OracleDriver";
        };
    }

    public void ensureTenantDataSource(String corpId) {
        if (tenantDataSources.containsKey(corpId))
            return;

        Corporation corp = corporationRepository.findById(Long.valueOf(corpId))
                .orElseThrow(() -> new RuntimeException("Corporación no encontrada: " + corpId));

        createAndRegisterDataSource(corp);
    }
}
