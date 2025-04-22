package com.nisimsoft.auth_system.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import com.nisimsoft.auth_system.entities.Corporation;
import com.nisimsoft.auth_system.entities.enums.NSCorpDBEngineEnum;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class TenantDataSourceProvider {

    private final Map<Object, Object> tenantDataSources = new HashMap<>();

    private final ObjectProvider<DataSource> dataSourceProvider;

    public Map<Object, Object> getTenantDataSources() {
        return tenantDataSources;
    }

    public void ensureTenantDataSource(String tenantId) {
        if (!tenantDataSources.containsKey(tenantId)) {
            Corporation corp = fetchCorporationById(tenantId);
            if (corp != null) {
                DataSource ds = createDataSourceForCorporation(corp);
                tenantDataSources.put(tenantId, ds);
            } else {
                throw new RuntimeException("No existe una corporación con ID: " + tenantId);
            }
        }
    }

    private Corporation fetchCorporationById(String tenantId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourceProvider.getObject());
        String sql = "SELECT * FROM ns_corp WHERE id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Corporation.class), tenantId)
                .stream().findFirst().orElse(null);
    }

    private DataSource createDataSourceForCorporation(Corporation corp) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(resolveDriver(corp.getDbEngine()));
        dataSource.setUrl(resolveJdbcUrl(corp));
        dataSource.setUsername(corp.getUsername());
        dataSource.setPassword(corp.getPassword());
        return dataSource;
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
}
