package ru.hse.api_gateway.service

import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.hse.api_gateway.client.rest.api.DatabaseProviderApi
import ru.hse.api_gateway.dto.UserDto
import java.util.*
import kotlin.io.encoding.ExperimentalEncodingApi

@Service
@ExperimentalEncodingApi
class UserService(
    private val defaultUserDetailsService: NewUserDetailsService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val databaseProviderApi: DatabaseProviderApi,
) {

    fun registerNewUser(name: String, email: String, password: String): ResponseEntity<String> {
        return try {
            val encodedPassword = passwordEncoder.encode(password)

            val response = databaseProviderApi.register(name, email, encodedPassword)

            if (response.statusCode.is2xxSuccessful) {
                val userDetails = defaultUserDetailsService.loadUserByEmailAndPassword(email, encodedPassword)
                val jwt = jwtService.generateToken(userDetails ?: throw Exception("User not found"))

                logger.info("User with email $email successfully registered")
                ResponseEntity.status(HttpStatus.CREATED).body(jwt)
            } else {
                response
            }
        } catch (e: FeignException) {
            logger.error("Feign error while registering user: ${e.status()}, ${e.contentUTF8()}", e)
            ResponseEntity.status(e.status()).body(e.contentUTF8())
        } catch (e: Exception) {
            logger.error("Error while registering user", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка при регистрации пользователя")
        }
    }

    fun login(email: String, password: String): ResponseEntity<String> {
        return try {
            val userSecurityDto = databaseProviderApi.login(email, password)
                ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Пользователь с таким email не найден")

            if (passwordEncoder.encode(password).equals(userSecurityDto.password)) {
                logger.error("Invalid password for user with email $email")
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неверный пароль")
            }

            val userDetails = defaultUserDetailsService.loadUserByEmailAndPassword(email, userSecurityDto.password)
            val jwt = jwtService.generateToken(userDetails ?: throw Exception("User not found"))

            logger.info("User with email $email successfully logged in")
            ResponseEntity.status(HttpStatus.OK).body(jwt)
        } catch (e: FeignException) {
            logger.error("Feign error while login user: ${e.status()}, ${e.contentUTF8()}", e)
            ResponseEntity.status(e.status()).body(e.contentUTF8())
        } catch (e: Exception) {
            logger.error("Error while login user", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка входа пользователя в систему")
        }
    }

    fun getUserInfo(userId: UUID): UserDto? {
        return try {
            val response = databaseProviderApi.getUserInfo(userId)
            response?.body
        } catch (e: Exception) {
            logger.error("Error while fetching user info", e)
            null
        }
    }

    fun updateUsername(userId: UUID, newUsername: String): String {
        return try {
            databaseProviderApi.updateUsername(newUsername, userId).body.toString()
        } catch (e: Exception) {
            logger.error("Error while updating username", e)
            ""
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UserService::class.java)
    }
}
