package com.nisimsoft.auth_system.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    private final Map<Object, Object> tenantDataSources = new ConcurrentHashMap<>();
    private final DataSource defaultDataSource;
    private final TenantDataSourceProvider tenantDataSourceProvider;

    public TenantRoutingDataSource(DataSource defaultDataSource, TenantDataSourceProvider tenantDataSourceProvider) {
        this.defaultDataSource = defaultDataSource;
        this.tenantDataSourceProvider = tenantDataSourceProvider;

        super.setDefaultTargetDataSource(defaultDataSource);
        super.setTargetDataSources(tenantDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContext.getTenant();

        if (tenantId == null) {
            System.out.println("⚠️  TenantContext no definido, usando base por defecto");
            return null;
        }

        System.out.println("🔎 Lookup tenant: " + tenantId);

        if (!tenantDataSources.containsKey(tenantId)) {
            try {
                DataSource ds = tenantDataSourceProvider.loadDataSourceForTenant(tenantId);
                if (ds != null) {
                    tenantDataSources.put(tenantId, ds);
                    super.setTargetDataSources(tenantDataSources);
                    super.afterPropertiesSet();
                }
            } catch (Exception ex) {
                System.err.println(
                        "❌ Error al obtener el DataSource para el tenant " + tenantId + ": " + ex.getMessage());
                return null;
            }
        }

        return tenantId;
    }
}