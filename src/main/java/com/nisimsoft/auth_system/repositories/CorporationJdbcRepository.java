package com.nisimsoft.auth_system.repositories;

import javax.sql.DataSource;

import com.nisimsoft.auth_system.datasource.TenantRoutingDataSource;
import com.nisimsoft.auth_system.entities.Corporation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

@Repository
@RequiredArgsConstructor
public class CorporationJdbcRepository {

    private final TenantRoutingDataSource routingDataSource;

    public void save(Corporation corp) {

        DataSource dataSource = routingDataSource.resolveCurrentDataSource();
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        String dbProductName = "";
        try {
            dbProductName = dataSource.getConnection().getMetaData().getDatabaseProductName().toLowerCase();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo determinar el motor de base de datos", e);
        }

        Long id;

        if (dbProductName.contains("mysql")) {
            // ✅ Simulación de secuencia en MySQL
            jdbc.update("UPDATE ns_corp_id_seq SET next_val = LAST_INSERT_ID(next_val + 1)");
            id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        } else if (dbProductName.contains("sql server")) {
            // ✅ SQL Server usa la secuencia real
            id = jdbc.queryForObject("SELECT NEXT VALUE FOR ns_corp_id_seq", Long.class);
        } else {
            throw new UnsupportedOperationException("Motor no soportado aún: " + dbProductName);
        }

        jdbc.update(
                "INSERT INTO ns_corp (id, db_engine, host, username, password, name) VALUES (?, ?, ?, ?, ?, ?)",
                id,
                corp.getDbEngine().name(),
                corp.getHost(),
                corp.getUsername(),
                corp.getPassword(),
                corp.getName());
    }
}