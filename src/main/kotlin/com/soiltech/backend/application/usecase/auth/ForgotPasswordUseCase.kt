package com.soiltech.backend.application.usecase.auth

import com.soiltech.backend.application.dto.auth.ForgotPasswordRequest
import com.soiltech.backend.domain.repository.UserRepository
import com.soiltech.backend.infrastructure.config.OtpProperties
import com.soiltech.backend.infrastructure.persistence.entity.OtpRecordJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.OtpRecordJpaRepository
import com.soiltech.backend.interfaces.exception.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ForgotPasswordUseCase(
    private val userRepository: UserRepository,
    private val otpRecordJpaRepository: OtpRecordJpaRepository,
    private val otpProperties: OtpProperties
) {
    private val log = LoggerFactory.getLogger(ForgotPasswordUseCase::class.java)

    @Transactional
    fun execute(request: ForgotPasswordRequest) {
        userRepository.findByEmail(request.email)
            ?: throw NotFoundException("No account found with this email")

        otpRecordJpaRepository.deleteByEmail(request.email)

        val otp = (100000..999999).random().toString()
        otpRecordJpaRepository.save(
            OtpRecordJpaEntity(
                email = request.email,
                code = otp,
                expiresAt = LocalDateTime.now().plusMinutes(otpProperties.expiryMinutes)
            )
        )

        // TODO: Send OTP via email/SMS provider
        log.info("OTP for {} is {} (delivery not implemented)", request.email, otp)
    }
}
