package com.soiltech.backend.application.usecase.customer

import com.soiltech.backend.application.dto.customer.CustomerProfileDto
import com.soiltech.backend.application.dto.customer.UpdateCustomerProfileRequest
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.interfaces.exception.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class GetCustomerProfileUseCase(
    private val customerProfileRepository: CustomerProfileRepository
) {
    fun execute(userId: UUID): CustomerProfileDto {
        return customerProfileRepository.findByUserId(userId)?.toDto()
            ?: throw NotFoundException("Customer profile not found")
    }
}

@Service
class UpdateCustomerProfileUseCase(
    private val customerProfileRepository: CustomerProfileRepository
) {
    @Transactional
    fun execute(userId: UUID, request: UpdateCustomerProfileRequest): CustomerProfileDto {
        val profile = customerProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Customer profile not found")

        val updated = profile.copy(
            fullName = request.fullName ?: profile.fullName,
            phone = request.phone ?: profile.phone,
            address = request.address ?: profile.address,
            profileImageUrl = request.profileImageUrl ?: profile.profileImageUrl,
            location = request.location ?: profile.location,
            updatedAt = LocalDateTime.now()
        )
        return customerProfileRepository.update(updated).toDto()
    }
}
