package ru.hse.api_gateway.service

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.hse.api_gateway.client.rest.api.DatabaseProviderApi
import ru.hse.api_gateway.model.User

@Service
class NewUserDetailsService(
    private val databaseProviderApi: DatabaseProviderApi
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails? {
        return databaseProviderApi.getUserByEmail(email)?.let{ userSecurityDto ->
            User(
                id = userSecurityDto.id,
                userName = userSecurityDto.username,
                email = userSecurityDto.email,
                password = "",
                role = userSecurityDto.roles.map { User.AuthorityType.valueOf(it) }
                    .firstOrNull() ?: User.AuthorityType.DEFAULT
            )
        }
    }

    fun loadUserByEmailAndPassword(email: String, password: String): UserDetails? {
        return databaseProviderApi.login(email, password)?.let { userSecurityDto ->
            User(
                id = userSecurityDto.id,
                userName = userSecurityDto.username,
                email = userSecurityDto.email,
                password = userSecurityDto.password,
                role = userSecurityDto.roles.map { User.AuthorityType.valueOf(it) }
                    .firstOrNull() ?: User.AuthorityType.DEFAULT
            )
        }
    }
}
