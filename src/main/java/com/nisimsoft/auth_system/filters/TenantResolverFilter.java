package com.nisimsoft.auth_system.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nisimsoft.auth_system.datasource.TenantContext;
import com.nisimsoft.auth_system.utils.GeneralUtils;
import com.nisimsoft.auth_system.utils.JwtUtils;

import java.io.IOException;
import java.util.List;

@Component
@Order(0) // Se ejecuta antes de cualquier otro filtro
@RequiredArgsConstructor
public class TenantResolverFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> EXCLUDED_PATHS = List.of(
            GeneralUtils.EXCLUDED_PATHS);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String path = request.getRequestURI();

            if (!isExcluded(path)) {
                String token = extractToken(request);
                if (token != null) {
                    String corpId = jwtUtils.extractClaim(token, claims -> claims.get("corpId", String.class));
                    if (corpId != null && !corpId.isBlank()) {
                        System.out.println("Tenant seteado desde TenantResolverFilter: " + corpId);
                        TenantContext.setTenant(corpId);
                    } else {
                        System.out.println("corpId no encontrado en token, usando default");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error en TenantResolverFilter: " + e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // Limpieza final
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    private boolean isExcluded(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
