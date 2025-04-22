package com.nisimsoft.auth_system.datasource;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nisimsoft.auth_system.responses.ErrorResponse;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;

@Component
@Order(Integer.MAX_VALUE) // Asegúrate de que este filtro se ejecute al final de la cadena de filtros
public class TenantContextCleanupFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            try {
                response.getWriter().write(new ObjectMapper().writeValueAsString(
                        new ErrorResponse("Unauthorized", "Invalid tenant context", request.getRequestURI())));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } finally {
            TenantContext.clear(); // ✅ Ahora sí se limpia al final de todo
        }
    }
}