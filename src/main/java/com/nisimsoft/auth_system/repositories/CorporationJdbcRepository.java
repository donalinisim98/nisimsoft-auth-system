package com.nisimsoft.auth_system.repositories;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.nisimsoft.auth_system.entities.Corporation;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CorporationJdbcRepository {
    // 👇 asegúrate que Spring inyecta el datasource principal
    // (TenantRoutingDataSource)
    @Qualifier("dataSource")
    private final DataSource dataSource;

    public void save(Corporation corp) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update(
                "INSERT INTO ns_corp (id,db_engine, host, username, password, name) VALUES (NEXT VALUE FOR ns_corp_id_seq,?, ?, ?, ?, ?)",
                corp.getDbEngine().name(),
                corp.getHost(),
                corp.getUsername(),
                corp.getPassword(),
                corp.getName());
    }
}
