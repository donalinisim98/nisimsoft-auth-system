package com.nisimsoft.auth_system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nisimsoft.auth_system.responses.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  @Autowired private final JwtUtils jwtUtils;

  private static final AntPathMatcher pathMatcher = new AntPathMatcher();

  private static final List<String> EXCLUDED_PATHS =
      List.of("/api/login", "/api/register", "/api/public/**");

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

      if (token != null && jwtUtils.extractEmail(token) != null) {
        String email = jwtUtils.extractEmail(token); // Valida y extrae email

        // Crea un objeto de autenticación
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(email, null, null);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // Guarda en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authToken);
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
