package com.nisimsoft.auth_system.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  @Autowired private final JwtUtils jwtUtils;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

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
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }
}
