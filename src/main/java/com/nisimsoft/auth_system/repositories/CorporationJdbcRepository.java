package com.nisimsoft.auth_system.repositories;

import javax.sql.DataSource;

import com.nisimsoft.auth_system.datasource.TenantContext;
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
        String tenantId = TenantContext.getTenant();
        System.out.println("🔍 Tenant en save(): " + tenantId);

        JdbcTemplate jdbc = new JdbcTemplate(routingDataSource.resolveCurrentDataSource());

        Long nextVal = jdbc.queryForObject("SELECT next_val FROM ns_corp_id_seq", Long.class);
        Long newVal = nextVal + 1;

        jdbc.update("UPDATE ns_corp_id_seq SET next_val = ?", newVal);

        Long id = newVal;

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