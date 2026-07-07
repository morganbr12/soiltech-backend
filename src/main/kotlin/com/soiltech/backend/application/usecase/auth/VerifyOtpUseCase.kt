package com.soiltech.backend.application.usecase.auth

import com.soiltech.backend.application.dto.auth.VerifyOtpRequest
import com.soiltech.backend.domain.repository.UserRepository
import com.soiltech.backend.infrastructure.persistence.jpa.OtpRecordJpaRepository
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.NotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class VerifyOtpUseCase(
    private val userRepository: UserRepository,
    private val otpRecordJpaRepository: OtpRecordJpaRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun execute(request: VerifyOtpRequest) {
        val otpRecord = otpRecordJpaRepository.findTopByEmailOrderByCreatedAtDesc(request.email)
            ?: throw BadRequestException("No OTP found for this email")

        if (otpRecord.used) throw BadRequestException("OTP has already been used")
        if (otpRecord.expiresAt.isBefore(LocalDateTime.now())) throw BadRequestException("OTP has expired")
        if (otpRecord.code != request.otp) throw BadRequestException("Invalid OTP")

        val user = userRepository.findByEmail(request.email)
            ?: throw NotFoundException("User not found")

        userRepository.save(user.copy(passwordHash = passwordEncoder.encode(request.newPassword)))

        otpRecord.used = true
        otpRecordJpaRepository.save(otpRecord)
    }
}
