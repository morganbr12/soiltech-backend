package com.soiltech.backend.application.usecase.auth

import com.soiltech.backend.application.dto.auth.AuthResponse
import com.soiltech.backend.application.dto.auth.LoginRequest
import com.soiltech.backend.domain.enum.UserRole
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

        val fullName = when (user.role) {
            UserRole.AGENT, UserRole.ADMIN ->
                agentProfileRepository.findByUserId(user.id)?.fullName ?: user.email
            UserRole.CUSTOMER ->
                customerProfileRepository.findByUserId(user.id)?.fullName ?: user.email
        }

        val accessToken = jwtService.generateAccessToken(user.id, user.email, user.role.name)
        val refreshToken = jwtService.generateRefreshToken(user.id, user.email, user.role.name)

        refreshTokenRepository.save(user.id, refreshToken, jwtProperties.refreshTokenExpiration)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = user.id.toString(),
            email = user.email,
            role = user.role.value,
            fullName = fullName
        )
    }
}
