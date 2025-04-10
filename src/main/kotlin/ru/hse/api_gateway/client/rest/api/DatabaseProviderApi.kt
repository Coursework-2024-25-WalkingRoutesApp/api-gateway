package ru.hse.api_gateway.client.rest.api

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.hse.api_gateway.dto.UserDto
import ru.hse.api_gateway.dto.UserSecurityDto
import java.util.*

@FeignClient(name = "database-provider", path = "\${feign.user-api.base-path}")
interface DatabaseProviderApi {

    @PostMapping("\${feign.user-api.endpoints.register}")
    fun register(
        @RequestParam("username") username: String,
        @RequestParam("email") email: String,
        @RequestParam("password") password: String
    ): ResponseEntity<String>

    @GetMapping("\${feign.user-api.endpoints.login}")
    fun login(
        @RequestParam("email") email: String,
        @RequestParam("password") password: String
    ): UserSecurityDto?

    @GetMapping("\${feign.user-api.endpoints.get-user-info}")
    fun getUserInfo(@RequestParam("userId") userId: UUID): ResponseEntity<UserDto>?

    @PutMapping("\${feign.user-api.endpoints.update-username}")
    fun updateUsername(
        @RequestParam("newUsername") newUsername: String,
        @RequestParam("userId") userId: UUID
    ): ResponseEntity<String>

    @GetMapping("\${feign.user-api.endpoints.get-user-by-email}")
    fun getUserByEmail(@RequestParam("email") email: String): UserSecurityDto?
}
