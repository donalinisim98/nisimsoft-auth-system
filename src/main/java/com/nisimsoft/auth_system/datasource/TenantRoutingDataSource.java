package com.nisimsoft.auth_system.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final Map<Object, Object> tenantDataSources = new ConcurrentHashMap<>();
    private final TenantDataSourceProvider tenantDataSourceProvider;

    public TenantRoutingDataSource(DataSource defaultDataSource, TenantDataSourceProvider tenantDataSourceProvider) {
        this.tenantDataSourceProvider = tenantDataSourceProvider;

        tenantDataSources.put("default", defaultDataSource);
        super.setDefaultTargetDataSource(defaultDataSource);
        super.setTargetDataSources(tenantDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContext.getTenant();
        if (tenantId == null) {
            System.out.println("TenantContext no definido, usando base por defecto");
            return "default";
        }

        System.out.println("Lookup tenant: " + tenantId);

        // ✅ Si no existe, lo agregamos y forzamos reload
        if (!tenantDataSources.containsKey(tenantId)) {
            try {
                DataSource ds = tenantDataSourceProvider.loadDataSourceForTenant(tenantId);
                tenantDataSources.put(tenantId, ds);

                // 🔁 Aquí está la CLAVE: forzamos la actualización interna
                super.setTargetDataSources(new HashMap<>(tenantDataSources));
                super.afterPropertiesSet();
            } catch (Exception e) {
                System.err.println("❌ Error al cargar DataSource para tenant " + tenantId + ": " + e.getMessage());
                return "default";
            }
        }

        return tenantId;
    }

    // ✅ Este método público nos permite acceder al DataSource activo
    public DataSource resolveCurrentDataSource() {
        return (DataSource) super.determineTargetDataSource();
    }
}
