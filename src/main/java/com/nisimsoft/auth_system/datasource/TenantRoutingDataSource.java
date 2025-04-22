package com.nisimsoft.auth_system.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final Map<Object, Object> tenantDataSources = new ConcurrentHashMap<>();
    private final TenantDataSourceProvider tenantDataSourceProvider;

    public TenantRoutingDataSource(DataSource defaultDataSource, TenantDataSourceProvider tenantDataSourceProvider) {
        this.tenantDataSourceProvider = tenantDataSourceProvider;

        // Inicializamos el map con al menos el default
        tenantDataSources.put("default", defaultDataSource);
        super.setDefaultTargetDataSource(defaultDataSource);
        super.setTargetDataSources(tenantDataSources); // <- evita el error
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContext.getTenant();

        if (tenantId == null) {
            System.out.println("⚠️ TenantContext no definido, usando base por defecto");
            return "default";
        }

        System.out.println("🔎 Lookup tenant: " + tenantId);

        tenantDataSources.computeIfAbsent(tenantId, id -> {
            try {
                return tenantDataSourceProvider.loadDataSourceForTenant(tenantId);
            } catch (Exception e) {
                System.err.println("❌ Error al cargar DataSource para tenant " + tenantId + ": " + e.getMessage());
                return null;
            }
        });

        return tenantId;
    }
}
