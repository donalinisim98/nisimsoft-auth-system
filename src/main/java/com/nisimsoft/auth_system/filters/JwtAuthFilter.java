package com.nisimsoft.auth_system.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nisimsoft.auth_system.responses.ErrorResponse;
import com.nisimsoft.auth_system.utils.GeneralUtils;
import com.nisimsoft.auth_system.utils.JwtUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1) // Asegúrate de que este filtro se ejecute antes de otros filtros de seguridad
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  @Autowired
  private final JwtUtils jwtUtils;

  private static final AntPathMatcher pathMatcher = new AntPathMatcher();

  private static final List<String> EXCLUDED_PATHS = List.of(GeneralUtils.EXCLUDED_PATHS);

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();

    if (isExcluded(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String token = extractToken(request); // "Bearer <token>" → "<token>"

      if (token != null) {
        String email = jwtUtils.extractClaim(token, Claims::getSubject);
        String corpId = jwtUtils.extractClaim(token, claims -> claims.get("corpId", String.class));

        if (email != null && corpId != null && !corpId.isBlank()) {
          // ✅ Este print te ayudará a ver si llega correctamente
          // System.out.println("✅ Tenant seteado desde token: " + corpId);
          // TenantContext.setTenant(corpId);

          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, null, null);
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        } else {
          System.out.println("⚠️ Token sin corpId válido. Se usará la base por defecto.");
        }
      }

      filterChain.doFilter(request, response); // Continúa la cadena de filtros
    } catch (ExpiredJwtException ex) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType("application/json");
      response
          .getWriter()
          .write(
              new ObjectMapper()
                  .writeValueAsString(
                      new ErrorResponse(
                          "Token expirado",
                          "El token JWT ha expirado. Por favor vuelve a iniciar sesión.",
                          request.getRequestURI())));
    } catch (Exception ex) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response
          .getWriter()
          .write(
              new ObjectMapper()
                  .writeValueAsString(
                      new ErrorResponse(
                          "Error de autenticación",
                          "Token inválido o no autorizado.",
                          request.getRequestURI())));
    }
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }

  private boolean isExcluded(String path) {
    return EXCLUDED_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
  }
}
