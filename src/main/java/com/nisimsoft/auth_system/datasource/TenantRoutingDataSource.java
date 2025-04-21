package com.nisimsoft.auth_system.datasource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * DataSource dinámico basado en el corpId del contexto del hilo.
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    // Mapa de corpId → DataSource
    private final Map<Object, Object> targetDataSources = new ConcurrentHashMap<>();

    // DataSource por defecto (base maestra donde está ns_corp)
    private final DataSource defaultDataSource;

    public TenantRoutingDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
        super.setDefaultTargetDataSource(defaultDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet(); // importante para inicializar
    }

    /**
     * Devuelve el corpId actual del hilo para enrutar al DataSource adecuado.
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getTenant(); // e.g. "2"
    }

    /**
     * Verifica si ya se tiene cargado el DataSource para un tenant específico.
     */
    public boolean hasTenant(String tenantId) {
        return targetDataSources.containsKey(tenantId);
    }

    /**
     * Agrega un nuevo tenant con su DataSource.
     */
    public void addTenant(String tenantId, DataSource dataSource) {
        targetDataSources.put(tenantId, dataSource);
        // Reasignamos fuentes para que AbstractRoutingDataSource las reconozca
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet(); // vuelve a inicializar internamente
    }

    /**
     * Devuelve el DataSource por defecto.
     */
    public DataSource getDefaultDataSource() {
        return defaultDataSource;
    }
}
