package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.auth.*
import com.soiltech.backend.application.usecase.auth.*
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val logoutUseCase: LogoutUseCase
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val data = loginUseCase.execute(request)
        return ResponseEntity.ok(ApiResponse.success(data, "Login successful"))
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val data = registerUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Registration successful"))
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val data = refreshTokenUseCase.execute(request)
        return ResponseEntity.ok(ApiResponse.success(data, "Token refreshed"))
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<ApiResponse<Unit?>> {
        forgotPasswordUseCase.execute(request)
        return ResponseEntity.ok(ApiResponse.success(null, "OTP sent to your email"))
    }

    @PostMapping("/verify-otp")
    fun verifyOtp(@Valid @RequestBody request: VerifyOtpRequest): ResponseEntity<ApiResponse<Unit?>> {
        verifyOtpUseCase.execute(request)
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"))
    }

    @PostMapping("/logout")
    fun logout(@Valid @RequestBody request: LogoutRequest): ResponseEntity<ApiResponse<Unit?>> {
        logoutUseCase.execute(request)
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"))
    }
}
