package com.nisimsoft.auth_system.datasource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Ruteador dinámico de DataSource por corpId (tenant).
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final Map<Object, Object> targetDataSources = new ConcurrentHashMap<>();
    private final DataSource defaultDataSource;
    private final TenantDataSourceProvider tenantDataSourceProvider;

    public TenantRoutingDataSource(DataSource defaultDataSource, TenantDataSourceProvider provider) {
        this.defaultDataSource = defaultDataSource;
        this.tenantDataSourceProvider = provider;
        super.setDefaultTargetDataSource(defaultDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        final String tenantId = TenantContext.getTenant();

        if (tenantId == null) {
            System.err.println("⚠️  TenantContext no definido, usando base por defecto");
            return null;
        }

        if (!targetDataSources.containsKey(tenantId)) {
            synchronized (this) {
                if (!targetDataSources.containsKey(tenantId)) {
                    try {
                        tenantDataSourceProvider.ensureTenantDataSource(tenantId);
                        DataSource tenantDs = (DataSource) tenantDataSourceProvider.getTenantDataSources()
                                .get(tenantId);
                        if (tenantDs != null) {
                            this.addTenant(tenantId, tenantDs); // registra dinámicamente
                        }
                    } catch (Exception e) {
                        System.err.println(
                                "❌ No se pudo cargar DataSource para tenant " + tenantId + ": " + e.getMessage());
                        return null;
                    }
                }
            }
        }

        return tenantId;
    }

    public void addTenant(String tenantId, DataSource dataSource) {
        targetDataSources.put(tenantId, dataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet(); // actualiza internamente
    }

    public DataSource getDefaultDataSource() {
        return defaultDataSource;
    }
}