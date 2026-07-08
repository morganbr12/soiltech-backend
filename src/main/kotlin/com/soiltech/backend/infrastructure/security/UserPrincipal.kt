package com.soiltech.backend.infrastructure.security

import com.soiltech.backend.domain.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

data class UserPrincipal(
    val id: UUID,
    val email: String,
    val role: String,
    private val password: String,
    private val active: Boolean,
    val permissions: Set<String> = emptySet()
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        buildList {
            add(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
            permissions.forEach { add(SimpleGrantedAuthority(it)) }
        }

    override fun getPassword(): String = password
    override fun getUsername(): String = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = active
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = active

    companion object {
        fun from(user: User, permissions: Set<String> = emptySet()): UserPrincipal = UserPrincipal(
            id = user.id,
            email = user.email,
            role = user.role.name,
            password = user.passwordHash,
            active = user.isActive,
            permissions = permissions
        )
    }
}
