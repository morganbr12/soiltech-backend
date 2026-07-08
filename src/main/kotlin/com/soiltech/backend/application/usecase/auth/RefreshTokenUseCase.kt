package com.soiltech.backend.application.usecase.auth

import com.soiltech.backend.application.dto.auth.AuthResponse
import com.soiltech.backend.application.dto.auth.RefreshTokenRequest
import com.soiltech.backend.domain.repository.RefreshTokenRepository
import com.soiltech.backend.domain.repository.UserRepository
import com.soiltech.backend.infrastructure.security.JwtProperties
import com.soiltech.backend.infrastructure.security.JwtService
import com.soiltech.backend.interfaces.exception.UnauthorizedException
import org.springframework.stereotype.Service

@Service
class RefreshTokenUseCase(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties
) {
    fun execute(request: RefreshTokenRequest): AuthResponse {
        if (!jwtService.isTokenValid(request.refreshToken)) {
            throw UnauthorizedException("Invalid or expired refresh token")
        }

        if (!refreshTokenRepository.isValid(request.refreshToken)) {
            throw UnauthorizedException("Refresh token has been revoked")
        }

        val userId = jwtService.extractUserId(request.refreshToken)
        val user = userRepository.findById(userId)
            ?: throw UnauthorizedException("User not found")

        if (!user.isActive) throw UnauthorizedException("Account is deactivated")

        val newAccessToken = jwtService.generateAccessToken(user.id, user.email, user.role.name)
        val newRefreshToken = jwtService.generateRefreshToken(user.id, user.email, user.role.name)

        refreshTokenRepository.save(user.id, newRefreshToken, jwtProperties.refreshTokenExpiration)

        return AuthResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = jwtProperties.accessTokenExpiration / 1000
        )
    }
}
