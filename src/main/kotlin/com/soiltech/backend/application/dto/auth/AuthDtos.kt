package com.soiltech.backend.application.dto.auth

import com.soiltech.backend.domain.enum.CustomerAccountType
import com.soiltech.backend.domain.enum.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank @field:Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number")
    val phone: String,
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

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val userId: String,
    val email: String,
    val role: String,
    val fullName: String
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
