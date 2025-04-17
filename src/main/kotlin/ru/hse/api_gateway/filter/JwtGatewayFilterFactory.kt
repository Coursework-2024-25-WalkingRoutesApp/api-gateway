package ru.hse.api_gateway.filter

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.hse.api_gateway.config.JwtProperties
import javax.crypto.SecretKey
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Component
@ExperimentalEncodingApi
class JwtGatewayFilterFactory(
    jwtProperties: JwtProperties
) : AbstractGatewayFilterFactory<JwtGatewayFilterFactory.Config>(Config::class.java) {

    private val secretKey: SecretKey = Keys.hmacShaKeyFor(Base64.decode(jwtProperties.secret))

    class Config(
        val excludeRoutes: List<String> = emptyList()
    )

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val request = exchange.request
            val path = request.uri.path

            if (config.excludeRoutes.any { path.startsWith(it) }) {
                logger.debug("Path '{}' is excluded from auth filter. Skipping.", path)
                return@GatewayFilter chain.filter(exchange)
            }

            val token = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
                ?.takeIf { it.startsWith("Bearer ") }
                ?.removePrefix("Bearer ")
                ?.trim()

            if (token.isNullOrBlank()) {
                logger.warn("Missing or malformed Authorization header")
                return@GatewayFilter unauthorized(exchange)
            }

            return@GatewayFilter runCatching { extractAllClaims(token) }
                .onFailure { ex ->
                    logger.warn("Invalid or expired token: {}", ex.message)
                    return@GatewayFilter unauthorized(exchange)
                }
                .getOrNull()
                ?.let { claims ->
                    val role = claims["role"] as? String
                    val userId = claims["userId"] as? String

                    logger.debug("Token claims extracted: userId={}, role={}", userId, role)

                    when {
                        path.contains("/admin/") && role != USER_ROLE_ADMIN -> {
                            logger.warn("Access denied to /admin for role '{}'", role)
                            return@GatewayFilter forbidden(exchange)
                        }
                        path.contains("/user/") && role !in USER_ROLES_SCOPE -> {
                            logger.warn("Access denied to /user for role '{}'", role)
                            return@GatewayFilter forbidden(exchange)
                        }
                    }

                    val mutatedRequest = request.mutate()
                        .header("X-User-Id", userId.orEmpty())
                        .header("X-Role", role.orEmpty())
                        .build()

                    val mutatedExchange = exchange.mutate().request(mutatedRequest).build()

                    logger.debug("Request authorized for userId={}, role={}", userId, role)
                    chain.filter(mutatedExchange)
                } ?: unauthorized(exchange)
        }
    }

    private fun unauthorized(exchange: ServerWebExchange): Mono<Void> {
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        return exchange.response.setComplete()
    }

    private fun forbidden(exchange: ServerWebExchange): Mono<Void> {
        exchange.response.statusCode = HttpStatus.FORBIDDEN
        return exchange.response.setComplete()
    }

    private fun extractAllClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(JwtGatewayFilterFactory::class.java)

        private val USER_ROLES_SCOPE = listOf("ADMIN", "DEFAULT")
        private const val USER_ROLE_ADMIN = "ADMIN"
    }
}
