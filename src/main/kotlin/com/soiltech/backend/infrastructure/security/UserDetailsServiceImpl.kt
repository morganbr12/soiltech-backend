package com.soiltech.backend.infrastructure.security

import com.soiltech.backend.domain.enum.UserRole
import com.soiltech.backend.infrastructure.persistence.jpa.AdminProfileJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.UserJpaRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

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
                ?: emptySet()
        } else {
            emptySet()
        }

        return UserPrincipal.from(user, permissions)
    }
}
