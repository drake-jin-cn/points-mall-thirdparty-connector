package com.pointsmall.thirdparty.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check endpoint (WebFlux reactive).
 *
 * Returns Mono<Map<String, Object>>:
 *   - Mono is WebFlux's async container representing "0 or 1 value"
 *   - Mono.just(value) wraps a synchronous value and emits it immediately
 *   - The Netty server subscribes to this Mono and serializes it into the HTTP response
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Mono<Map<String, Object>> health() {
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        body.put("service", "points-mall-thirdparty-connector");
        body.put("timestamp", Instant.now().toString());
        body.put("db", "ok"); // No direct DB connection in this phase
        body.put("uptime", uptimeSeconds);

        return Mono.just(body);
    }
}
