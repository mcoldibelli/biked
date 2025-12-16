package dev.mcoldibelli.biked.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtConfig(
    String secret,
    long expiration
) {

}
