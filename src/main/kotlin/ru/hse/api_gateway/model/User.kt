package ru.hse.api_gateway.model

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

data class User(
    val id: UUID? = null,

    var userName: String,

    private var email: String,

    private var password: String,

    val role: AuthorityType? = AuthorityType.DEFAULT
) : UserDetails {

    override fun getAuthorities(): MutableList<AuthorityType?> = mutableListOf(role)

    fun setEmail(email: String) {
        this.email = email
    }

    override fun getUsername() = email

    fun setPassword(password: String) {
        this.password = password
    }

    fun getName() = userName

    fun setName(userName: String) {
        this.userName = userName
    }

    override fun getPassword() = password

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true

    enum class AuthorityType : GrantedAuthority {
        ADMIN,
        DEFAULT;

        override fun getAuthority() = this.name
    }
}
