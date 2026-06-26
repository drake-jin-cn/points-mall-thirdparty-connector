package com.pointsmall.thirdparty.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for JwtAuthWebFilter.
 *
 * <p>Uses WebEnvironment.RANDOM_PORT + @LocalServerPort to build WebTestClient manually, which is
 * the recommended pattern in Spring Boot 4.x.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class JwtAuthWebFilterTest {

  private static final String SECRET = "dev-insecure-secret-at-least-32-chars!!";
  private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

  @LocalServerPort private int port;

  private WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  private String validToken() {
    return Jwts.builder()
        .subject("42")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 60_000))
        .signWith(KEY)
        .compact();
  }

  private String expiredToken() {
    return Jwts.builder()
        .subject("42")
        .issuedAt(new Date(System.currentTimeMillis() - 120_000))
        .expiration(new Date(System.currentTimeMillis() - 60_000))
        .signWith(KEY)
        .compact();
  }

  private String wrongKeyToken() {
    SecretKey wrongKey =
        Keys.hmacShaKeyFor("wrong-secret-key-that-is-at-least-32-chars!".getBytes());
    return Jwts.builder()
        .subject("42")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 60_000))
        .signWith(wrongKey)
        .compact();
  }

  @Test
  void validToken_passesThrough() {
    webTestClient
        .get()
        .uri("/health")
        .header("Authorization", "Bearer " + validToken())
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void healthEndpoint_noToken_returns200() {
    webTestClient.get().uri("/health").exchange().expectStatus().isOk();
  }

  @Test
  void missingAuthHeader_returns401() {
    webTestClient
        .get()
        .uri("/api/protected")
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.code")
        .isEqualTo("tpc-7001")
        .jsonPath("$.message")
        .isEqualTo("Unauthorized")
        .jsonPath("$.data")
        .isEmpty();
  }

  @Test
  void invalidSignature_returns401() {
    webTestClient
        .get()
        .uri("/api/protected")
        .header("Authorization", "Bearer " + wrongKeyToken())
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.code")
        .isEqualTo("tpc-7001");
  }

  @Test
  void expiredToken_returns401() {
    webTestClient
        .get()
        .uri("/api/protected")
        .header("Authorization", "Bearer " + expiredToken())
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.code")
        .isEqualTo("tpc-7001");
  }

  @Test
  void malformedToken_returns401() {
    webTestClient
        .get()
        .uri("/api/protected")
        .header("Authorization", "Bearer not.a.valid.jwt.token")
        .exchange()
        .expectStatus()
        .isUnauthorized()
        .expectBody()
        .jsonPath("$.code")
        .isEqualTo("tpc-7001");
  }
}
