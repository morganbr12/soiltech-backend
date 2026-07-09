package com.soiltech.backend.infrastructure.security

import com.soiltech.backend.domain.enum.UserRole
import com.soiltech.backend.infrastructure.persistence.jpa.AdminProfileJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.UserJpaRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

private val ALL_ADMIN_PERMISSIONS: Set<String> = setOf(
    "agents:view", "agents:create", "agents:edit", "agents:delete",
    "customers:view", "customers:create", "customers:edit", "customers:delete",
    "customers:verify", "customers:suspend", "customers:analytics",
    "customers:orders", "customers:wallet", "customers:reviews",
    "customers:notifications", "customers:chats",
    "farmers:view", "farmers:create", "farmers:edit", "farmers:delete", "farmers:approve",
    "lbc:view", "lbc:create", "lbc:edit", "lbc:delete", "lbc:suspend",
    "users:view", "users:create",
    "roles:manage", "settings:view"
)

@Service
class UserDetailsServiceImpl(
    private val userJpaRepository: UserJpaRepository,
    private val adminProfileJpaRepository: AdminProfileJpaRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val userEntity = userJpaRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        val user = userEntity.toDomain()

        val permissions: Set<String> = if (user.role == UserRole.ADMIN) {
            adminProfileJpaRepository.findByUserId(user.id)
                ?.adminRole?.permissions
                ?: ALL_ADMIN_PERMISSIONS
        } else {
            emptySet()
        }

        return UserPrincipal.from(user, permissions)
    }
}
