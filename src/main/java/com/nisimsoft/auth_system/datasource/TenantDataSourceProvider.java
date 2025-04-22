package com.nisimsoft.auth_system.datasource;

import com.nisimsoft.auth_system.entities.Corporation;
import com.nisimsoft.auth_system.entities.enums.NSCorpDBEngineEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Carga dinámicamente los DataSources de cada tenant (corporación) desde la
 * base principal.
 */
@Component
@RequiredArgsConstructor
public class TenantDataSourceProvider {

    @Qualifier("defaultDataSource")
    private final DataSource defaultDataSource;

    private final Map<Object, Object> tenantDataSources = new HashMap<>();

    public Map<Object, Object> getTenantDataSources() {
        return tenantDataSources;
    }

    public void ensureTenantDataSource(String corpId) {
        if (tenantDataSources.containsKey(corpId))
            return;

        // ✅ Usa la base de datos principal para obtener los datos del tenant
        JdbcTemplate jdbcTemplate = new JdbcTemplate(defaultDataSource);

        String sql = "SELECT id, host, username, password, name, logo, db_engine FROM ns_corp WHERE id = ?";

        Corporation corp = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Corporation c = new Corporation();
            c.setId(rs.getLong("id"));
            c.setHost(rs.getString("host"));
            c.setUsername(rs.getString("username"));
            c.setPassword(rs.getString("password"));
            c.setName(rs.getString("name"));
            c.setLogo(rs.getString("logo"));
            c.setDbEngine(NSCorpDBEngineEnum.valueOf(rs.getString("db_engine")));
            return c;
        }, Long.valueOf(corpId)); // 👈 vararg correcto

        createAndRegisterDataSource(corp);
    }

    private void createAndRegisterDataSource(Corporation corp) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setUrl(resolveJdbcUrl(corp));
        dataSource.setUsername(corp.getUsername());
        dataSource.setPassword(corp.getPassword());
        dataSource.setDriverClassName(resolveDriver(corp.getDbEngine()));

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
}