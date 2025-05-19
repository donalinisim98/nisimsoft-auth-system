package com.nisimsoft.auth_system.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.lang.Function;
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

  public String generateToken(String email, String corpId) {
    Key key = getSigningKey(); // Obtiene la clave HMAC desde .env

    return Jwts.builder()
        .subject(email) // Establece el email como "subject"
        .claim("corpId", corpId) // nuevo claim
        .issuedAt(Date.from(Instant.now())) // Fecha de emisión
        .expiration(Date.from(Instant.now().plusMillis(expirationTime)))
        .signWith(key) // Firma el token con HMAC-SHA
        .compact(); // Convierte a String
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    SecretKey key = getSigningKey();
    Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    return claimsResolver.apply(claims);
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes());
  }
}
