package com.soiltech.backend.infrastructure.security

import com.soiltech.backend.infrastructure.persistence.jpa.UserJpaRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userJpaRepository: UserJpaRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userJpaRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        return UserPrincipal.from(user.toDomain())
    }
}
