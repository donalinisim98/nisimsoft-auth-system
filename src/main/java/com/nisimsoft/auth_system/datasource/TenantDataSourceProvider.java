package com.nisimsoft.auth_system.datasource;

import com.nisimsoft.auth_system.entities.Corporation;
import com.nisimsoft.auth_system.entities.enums.NSCorpDBEngineEnum;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TenantDataSourceProvider {

    private final DataSource defaultDataSource;
    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();

    public TenantDataSourceProvider(@Qualifier("defaultDataSource") DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public DataSource loadDataSourceForTenant(String tenantId) {
        return tenantDataSources.computeIfAbsent(tenantId, id -> {
            Corporation corp = fetchCorporationById(id);

            if (corp == null) {
                throw new RuntimeException("Corporación no encontrada con ID: " + tenantId);
            }

            return createDataSourceForCorporation(corp);
        });
    }

    private Corporation fetchCorporationById(String tenantId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(defaultDataSource);
        String sql = "SELECT * FROM ns_corp WHERE id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Corporation.class), tenantId)
                .stream()
                .findFirst()
                .orElse(null);
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
            case MSSQL -> "jdbc:sqlserver://" + corp.getHost() + ";databaseName=" + corp.getDbName()
                    + ";encrypt=false;trustServerCertificate=true";
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
