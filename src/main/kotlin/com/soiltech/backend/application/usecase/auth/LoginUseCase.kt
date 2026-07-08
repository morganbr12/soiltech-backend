package com.soiltech.backend.application.usecase.auth

import com.soiltech.backend.application.dto.auth.AuthResponse
import com.soiltech.backend.application.dto.auth.AuthUserDto
import com.soiltech.backend.application.dto.auth.LoginRequest
import com.soiltech.backend.domain.enum.UserRole
import com.soiltech.backend.domain.repository.AdminProfileRepository
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.domain.repository.RefreshTokenRepository
import com.soiltech.backend.domain.repository.UserRepository
import com.soiltech.backend.infrastructure.security.JwtProperties
import com.soiltech.backend.infrastructure.security.JwtService
import com.soiltech.backend.interfaces.exception.UnauthorizedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class LoginUseCase(
    private val userRepository: UserRepository,
    private val adminProfileRepository: AdminProfileRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val customerProfileRepository: CustomerProfileRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
    private val passwordEncoder: PasswordEncoder
) {
    fun execute(request: LoginRequest): AuthResponse {
        val user = userRepository.findByPhone(request.phone)
            ?: throw UnauthorizedException("Invalid phone number or password")

        if (!user.isActive) throw UnauthorizedException("Account is deactivated")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid phone number or password")
        }

        val fullName: String
        val region: String?
        when (user.role) {
            UserRole.ADMIN -> {
                val profile = adminProfileRepository.findByUserId(user.id)
                fullName = profile?.fullName ?: user.email
                region = profile?.region
            }
            UserRole.AGENT -> {
                val profile = agentProfileRepository.findByUserId(user.id)
                fullName = profile?.fullName ?: user.email
                region = profile?.region
            }
            UserRole.CUSTOMER -> {
                fullName = customerProfileRepository.findByUserId(user.id)?.fullName ?: user.email
                region = null
            }
        }

        val accessToken = jwtService.generateAccessToken(user.id, user.email, user.role.name)
        val refreshToken = jwtService.generateRefreshToken(user.id, user.email, user.role.name)

        refreshTokenRepository.save(user.id, refreshToken, jwtProperties.refreshTokenExpiration)

        return AuthResponse(
            user = AuthUserDto(
                id = user.id.toString(),
                email = user.email,
                firstName = fullName.substringBefore(" ").ifBlank { fullName },
                lastName = fullName.substringAfter(" ", ""),
                fullName = fullName,
                phone = user.phone,
                role = user.role.value,
                status = if (user.isActive) "active" else "inactive",
                region = region,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            ),
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtProperties.accessTokenExpiration / 1000
        )
    }
}
