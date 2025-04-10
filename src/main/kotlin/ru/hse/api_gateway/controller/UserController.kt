package ru.hse.api_gateway.controller

import GET_USER_INFO_URL
import LOGIN_URL
import REGISTER_URL
import UPDATE_USERNAME_URL
import USER_BASE_PATH_URL
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.hse.api_gateway.model.User
import ru.hse.api_gateway.service.UserService
import java.util.*
import kotlin.io.encoding.ExperimentalEncodingApi

@RestController
@RequestMapping(USER_BASE_PATH_URL)
@ExperimentalEncodingApi
class UserController(
    private val userService: UserService
) {
    @PostMapping(REGISTER_URL)
    fun register(
        @RequestParam username: String,
        @RequestParam email: String,
        @RequestParam password: String
    ): ResponseEntity<String> =
        userService.registerNewUser(username, email, password)

    @GetMapping(LOGIN_URL)
    fun login(@RequestParam email: String, @RequestParam password: String): ResponseEntity<String> =
        userService.login(email, password)

    @GetMapping(GET_USER_INFO_URL)
    fun getUserInfo(@AuthenticationPrincipal user: User?): ResponseEntity<Any> {
        return withUserId(user) { userId ->
            ResponseEntity.ok(userService.getUserInfo(userId))
        }
    }

    @PutMapping(UPDATE_USERNAME_URL)
    fun updateUsername(
        @AuthenticationPrincipal user: User?,
        @RequestParam newUsername: String
    ): ResponseEntity<Any> {
        return withUserId(user) { userId ->
            ResponseEntity.ok(userService.updateUsername(userId, newUsername))
        }
    }

    private fun withUserId(user: User?, block: (UUID) -> ResponseEntity<Any>): ResponseEntity<Any> {
        val userId = user?.id
        return if (userId != null) {
            block(userId)
        } else {
            logger.error("User ID is null")
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token")
        }
    }

    companion object {
        private val  logger = org.slf4j.LoggerFactory.getLogger(UserController::class.java)
    }
}
