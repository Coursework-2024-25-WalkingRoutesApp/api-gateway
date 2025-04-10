package ru.hse.api_gateway.filter

import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ru.hse.api_gateway.service.JwtService
import kotlin.io.encoding.ExperimentalEncodingApi

@Component
@ExperimentalEncodingApi
class JwtRequestFilter(
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        val jwt = authHeader
            ?.takeIf { it.startsWith("Bearer ") }
            ?.substring(7)

        val username = jwt
            ?.let { token -> runCatching { jwtService.extractUsername(token) }.getOrNull() }

        if (username != null && SecurityContextHolder.getContext().authentication == null) {
            try {
                val userDetails = userDetailsService.loadUserByUsername(username)
                    ?: throw JwtException("User not found")

                if (!jwtService.validateToken(jwt)) {
                    throw JwtException("Invalid JWT token")
                }

                val authToken = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                ).apply {
                    details = WebAuthenticationDetailsSource().buildDetails(request)
                }

                SecurityContextHolder.getContext().authentication = authToken

            } catch (e: JwtException) {
                logger.error("JWT token is invalid: ${e.message}")
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.message)
                return
            }
        }

        chain.doFilter(request, response)
    }
}
