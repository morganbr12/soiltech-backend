package com.soiltech.backend.application.dto.auth

import com.soiltech.backend.domain.enum.CustomerAccountType
import com.soiltech.backend.domain.enum.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class LoginRequest(
    @field:Email
    val email: String? = null,
    @field:Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number")
    val phone: String? = null,
    @field:NotBlank
    val password: String
)

data class RegisterRequest(
    @field:NotBlank @field:Email
    val email: String,
    @field:NotBlank @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
    @field:NotBlank
    val fullName: String,
    @field:NotBlank @field:Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number")
    val phone: String,
    val role: UserRole = UserRole.AGENT,
    val region: String? = null,
    val accountType: CustomerAccountType = CustomerAccountType.INDIVIDUAL,
    val location: String? = null
)

data class AuthUserDto(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val phone: String,
    val role: String,
    val status: String,
    val region: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AuthResponse(
    val user: AuthUserDto,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

data class RefreshTokenRequest(
    @field:NotBlank
    val refreshToken: String
)

data class ForgotPasswordRequest(
    @field:NotBlank @field:Email
    val email: String
)

data class VerifyOtpRequest(
    @field:NotBlank @field:Email
    val email: String,
    @field:NotBlank
    val otp: String,
    @field:NotBlank @field:Size(min = 8, message = "Password must be at least 8 characters")
    val newPassword: String
)

data class LogoutRequest(
    @field:NotBlank
    val refreshToken: String
)
