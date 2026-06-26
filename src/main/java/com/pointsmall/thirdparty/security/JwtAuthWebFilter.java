package com.pointsmall.thirdparty.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Reactive WebFilter that validates JWT Bearer tokens on all routes except /health.
 *
 * <p>Flow: - Skip /health (no auth required) - Read Authorization header - Missing or non-Bearer ->
 * 401 - Verify JWT signature + expiry (JJWT) - Invalid -> 401 with { code, message, data } - Valid
 * -> set userId attribute and continue chain
 */
public class JwtAuthWebFilter implements WebFilter {

  private static final String HEALTH_PATH = "/health";
  private static final String UNAUTHORIZED_JSON =
      "{\"code\":\"tpc-7001\",\"message\":\"Unauthorized\",\"data\":null}";

  private final SecretKey signingKey;

  public JwtAuthWebFilter(String jwtSecret) {
    this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String path = exchange.getRequest().getPath().value();

    if (HEALTH_PATH.equals(path)) {
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return unauthorized(exchange);
    }

    String token = authHeader.substring(7);
    try {
      var claims =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
      exchange.getAttributes().put("userId", claims.getSubject());
      return chain.filter(exchange);
    } catch (JwtException | IllegalArgumentException e) {
      return unauthorized(exchange);
    }
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
    byte[] bytes = UNAUTHORIZED_JSON.getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
    return exchange.getResponse().writeWith(Mono.just(buffer));
  }
}
