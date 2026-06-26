package com.pointsmall.thirdparty.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

/** Registers the JWT WebFilter as a Spring Bean. */
@Configuration
public class SecurityConfig {

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  @Bean
  public WebFilter jwtAuthWebFilter() {
    return new JwtAuthWebFilter(jwtSecret);
  }
}
