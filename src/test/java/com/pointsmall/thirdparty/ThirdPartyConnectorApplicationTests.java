package com.pointsmall.thirdparty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * WebTestClient is the WebFlux-specific test client.
 *
 * <p>Spring Boot 4.x: @AutoConfigureWebTestClient was removed. Use webEnvironment = RANDOM_PORT
 * with @LocalServerPort to build WebTestClient manually — this is the recommended pattern.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ThirdPartyConnectorApplicationTests {

  @LocalServerPort private int port;

  private WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
  }

  @Test
  void contextLoads() {}

  @Test
  void healthReturns200() {
    webTestClient
        .get()
        .uri("/health")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.status")
        .isEqualTo("ok");
  }
}
