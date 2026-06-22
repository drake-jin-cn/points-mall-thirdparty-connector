package com.pointsmall.thirdparty.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 健康检查端点。
 *
 * 注意返回类型是 Mono<Map<String, String>> 而不是普通的 Map：
 *   - Mono 代表"一个异步的、未来会产生 0 或 1 个值的容器"
 *   - Mono.just(value) 相当于把一个已有的值包装成异步形式立即返回
 *   - WebFlux 运行时（Netty）会订阅这个 Mono，拿到值后序列化成 JSON 响应
 *
 * 对调用方来说，HTTP 响应完全一样：200 { "status": "ok" }
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Mono<Map<String, String>> health() {
        // Mono.just() —— 把一个同步值包装进 Mono，立即可用
        return Mono.just(Map.of("status", "ok"));
    }
}
