package com.soiltech.backend.application.usecase.auth

import com.soiltech.backend.application.dto.auth.AuthResponse
import com.soiltech.backend.application.dto.auth.AuthRoleDto
import com.soiltech.backend.application.dto.auth.LoginRequest
import com.soiltech.backend.domain.enum.UserRole
import com.soiltech.backend.domain.repository.AdminProfileRepository
import com.soiltech.backend.domain.repository.RefreshTokenRepository
import com.soiltech.backend.domain.repository.UserRepository
import com.soiltech.backend.infrastructure.security.JwtProperties
import com.soiltech.backend.infrastructure.security.JwtService
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.UnauthorizedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class LoginUseCase(
    private val userRepository: UserRepository,
    private val adminProfileRepository: AdminProfileRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val jwtProperties: JwtProperties,
    private val passwordEncoder: PasswordEncoder
) {
    fun execute(request: LoginRequest): AuthResponse {
        val user = when {
            request.email != null -> {
                val found = userRepository.findByEmail(request.email)
                    ?: throw UnauthorizedException("Invalid email or password")
                if (found.role != UserRole.ADMIN)
                    throw UnauthorizedException("Invalid email or password")
                found
            }
            request.phone != null -> {
                val found = userRepository.findByPhone(request.phone)
                    ?: throw UnauthorizedException("Invalid phone number or password")
                if (found.role == UserRole.ADMIN)
                    throw UnauthorizedException("Admin accounts must log in with email")
                found
            }
            else -> throw BadRequestException("Email or phone number is required")
        }

        if (!user.isActive) throw UnauthorizedException("Account is deactivated")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            val msg = if (request.email != null) "Invalid email or password" else "Invalid phone number or password"
            throw UnauthorizedException(msg)
        }

        val accessToken = jwtService.generateAccessToken(user.id, user.email, user.role.name)
        val refreshToken = jwtService.generateRefreshToken(user.id, user.email, user.role.name)

        refreshTokenRepository.save(user.id, refreshToken, jwtProperties.refreshTokenExpiration)

        val permissions = if (user.role == UserRole.ADMIN)
            adminProfileRepository.findByUserId(user.id)?.permissions?.toList() ?: emptyList()
        else emptyList()

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtProperties.accessTokenExpiration / 1000,
            role = AuthRoleDto(name = user.role.name, value = user.role.value, permissions = permissions)
        )
    }
}
