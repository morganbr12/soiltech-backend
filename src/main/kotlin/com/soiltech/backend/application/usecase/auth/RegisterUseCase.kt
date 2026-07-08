package com.soiltech.backend.application.usecase.auth

import com.soiltech.backend.application.dto.auth.AuthResponse
import com.soiltech.backend.application.dto.auth.RegisterRequest
import com.soiltech.backend.domain.entity.AgentProfile
import com.soiltech.backend.domain.entity.CustomerProfile
import com.soiltech.backend.domain.entity.User
import com.soiltech.backend.domain.enum.UserRole
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.domain.repository.RefreshTokenRepository
import com.soiltech.backend.domain.repository.UserRepository
import com.soiltech.backend.infrastructure.persistence.jpa.AgentProfileJpaRepository
import com.soiltech.backend.infrastructure.security.JwtProperties
import com.soiltech.backend.infrastructure.security.JwtService
import com.soiltech.backend.interfaces.exception.ConflictException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class RegisterUseCase(
    private val userRepository: UserRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val customerProfileRepository: CustomerProfileRepository,
    private val agentProfileJpaRepository: AgentProfileJpaRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun execute(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("Email already registered")
        }
        if (userRepository.existsByPhone(request.phone)) {
            throw ConflictException("Phone number already registered")
        }

        val now = LocalDateTime.now()
        val userId = UUID.randomUUID()

        val user = userRepository.save(
            User(
                id = userId,
                email = request.email,
                phone = request.phone,
                passwordHash = passwordEncoder.encode(request.password),
                role = request.role,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )

        when (request.role) {
            UserRole.CUSTOMER -> customerProfileRepository.save(
                CustomerProfile(
                    id = UUID.randomUUID(),
                    userId = user.id,
                    fullName = request.fullName,
                    phone = request.phone,
                    address = null,
                    profileImageUrl = null,
                    accountType = request.accountType,
                    location = request.location,
                    createdAt = now,
                    updatedAt = now
                )
            )
            else -> agentProfileRepository.save(
                AgentProfile(
                    id = UUID.randomUUID(),
                    userId = user.id,
                    fullName = request.fullName,
                    agentCode = generateAgentCode(agentProfileJpaRepository),
                    region = request.region,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }

        val accessToken = jwtService.generateAccessToken(user.id, user.email, user.role.name)
        val refreshToken = jwtService.generateRefreshToken(user.id, user.email, user.role.name)

        refreshTokenRepository.save(user.id, refreshToken, jwtProperties.refreshTokenExpiration)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtProperties.accessTokenExpiration / 1000
        )
    }

    private fun generateAgentCode(jpaRepo: AgentProfileJpaRepository): String {
        var code: String
        do {
            code = "AGT-${(100000..999999).random()}"
        } while (jpaRepo.existsByAgentCode(code))
        return code
    }
}
