package com.nisimsoft.auth_system.datasource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nisimsoft.auth_system.config.JwtUtils;
import java.io.IOException;

@Component
@Order(0) // 🔥 MUY importante, se ejecuta antes que cualquier filtro
@RequiredArgsConstructor
public class TenantResolverFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractToken(request);
            if (token != null) {
                String corpId = jwtUtils.extractClaim(token, claims -> claims.get("corpId", String.class));
                if (corpId != null && !corpId.isBlank()) {
                    System.out.println("🏷️ Tenant seteado desde TenantResolverFilter: " + corpId);
                    TenantContext.setTenant(corpId);
                } else {
                    System.out.println("⚠️ corpId no encontrado en token, usando default");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error en TenantResolverFilter: " + e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // 🧹 Asegúrate de limpiar después
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }
}
