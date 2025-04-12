package ru.hse.api_gateway.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "jwt")
class JwtProperties (
    var secret: String
)

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfiguration {}