package dev.mcoldibelli.biked.service;

import dev.mcoldibelli.biked.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtConfig jwtConfig;

  public String generateToken(UUID userId, String email) {
    var now = new Date();
    var expiration = new Date(now.getTime() + jwtConfig.expiration());

    return Jwts.builder()
        .subject(userId.toString())
        .claim("email", email)
        .issuedAt(now)
        .expiration(expiration)
        .signWith(getSigningKey())
        .compact();
  }

  public UUID extractUserId(String token) {
    var claims = extractClaims(token);
    return UUID.fromString(claims.getSubject());
  }

  public String extractEmail(String token) {
    var claims = extractClaims(token);
    return claims.get("email", String.class);
  }

  public boolean isTokenValid(String token) {
    try {
      var claims = extractClaims(token);
      return !claims.getExpiration().before(new Date());
    } catch (Exception e) {
      return false;
    }
  }

  private Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = jwtConfig.secret().getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
