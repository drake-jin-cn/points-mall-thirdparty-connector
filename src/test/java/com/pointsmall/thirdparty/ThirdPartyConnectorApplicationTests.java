package com.pointsmall.thirdparty;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * WebTestClient 是 WebFlux 专用的测试客户端。
 * Spring Boot 4.x 中 @AutoConfigureWebTestClient 已移除；
 * 改用 webEnvironment = RANDOM_PORT，WebTestClient 会自动注入。
 * 用法与 MockMvc 类似，但支持响应式断言：
 *   .expectStatus().isOk()
 *   .expectBody(Map.class).consumeWith(result -> ...)
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ThirdPartyConnectorApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void contextLoads() {
    }

    @Test
    void healthReturns200() {
        webTestClient.get().uri("/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("ok");
    }
}
