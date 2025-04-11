package com.nisimsoft.auth_system.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
  @Value("${jwt.secret-key}")
  private String secretKey;

  @Value("${jwt.expiration-time}")
  private long expirationTime;

  public String generateToken(String email) {
    Key key = getSigningKey(); // Obtiene la clave HMAC desde .env

    return Jwts.builder()
        .subject(email) // Establece el email como "subject"
        .issuedAt(Date.from(Instant.now())) // Fecha de emisión
        .expiration(Date.from(Instant.now().plusMillis(expirationTime)))
        .signWith(key) // Firma el token con HMAC-SHA
        .compact(); // Convierte a String
  }

  public String extractEmail(String token) {
    SecretKey key = getSigningKey();

    Jws<Claims> claimsJws =
        Jwts.parser()
            .verifyWith(key) // Usar SecretKey
            .build()
            .parseSignedClaims(token); // Parsea el token

    return claimsJws.getPayload().getSubject(); // Extrae el email
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes());
  }
}
