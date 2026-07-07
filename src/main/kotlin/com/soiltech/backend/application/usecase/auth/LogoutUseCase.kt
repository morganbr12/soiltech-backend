package com.soiltech.backend.application.usecase.auth

import com.soiltech.backend.application.dto.auth.LogoutRequest
import com.soiltech.backend.domain.repository.RefreshTokenRepository
import org.springframework.stereotype.Service

@Service
class LogoutUseCase(
    private val refreshTokenRepository: RefreshTokenRepository
) {
    fun execute(request: LogoutRequest) {
        refreshTokenRepository.deleteByToken(request.refreshToken)
    }
}
