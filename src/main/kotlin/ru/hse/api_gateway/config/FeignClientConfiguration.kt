package ru.hse.api_gateway.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@Configuration
@EnableFeignClients(basePackages = ["ru.hse.api_gateway.client.rest.api"])
class FeignClientConfiguration
