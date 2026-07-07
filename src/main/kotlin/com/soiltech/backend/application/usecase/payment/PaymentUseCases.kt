package com.soiltech.backend.application.usecase.payment

import com.soiltech.backend.application.dto.payment.CreatePaymentRecordRequest
import com.soiltech.backend.application.dto.payment.PaymentRecordDto
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.domain.entity.PaymentRecord
import com.soiltech.backend.domain.enum.PaymentStatus
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.domain.repository.FarmerRepository
import com.soiltech.backend.domain.repository.PaymentRecordRepository
import com.soiltech.backend.interfaces.exception.ForbiddenException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class CreatePaymentRecordUseCase(
    private val paymentRecordRepository: PaymentRecordRepository,
    private val farmerRepository: FarmerRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    @Transactional
    fun execute(request: CreatePaymentRecordRequest, userId: UUID): PaymentRecordDto {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val farmer = farmerRepository.findById(request.farmerId)
            ?: throw NotFoundException("Farmer not found")
        if (farmer.agentId != agent.id) throw ForbiddenException("Access denied")

        val now = LocalDateTime.now()
        val payment = paymentRecordRepository.save(
            PaymentRecord(
                id = UUID.randomUUID(),
                farmerId = request.farmerId,
                agentId = agent.id,
                produceRecordId = request.produceRecordId,
                amount = request.amount,
                currency = request.currency,
                status = if (request.paidAt != null) PaymentStatus.COMPLETED else PaymentStatus.PENDING,
                reference = request.reference,
                paidAt = request.paidAt,
                createdAt = now,
                updatedAt = now
            )
        )
        return payment.toDto()
    }
}

@Service
class ListPaymentRecordsUseCase(
    private val paymentRecordRepository: PaymentRecordRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    fun execute(
        userId: UUID,
        farmerId: UUID?,
        status: PaymentStatus?,
        page: Int,
        perPage: Int
    ): Pair<List<PaymentRecordDto>, PaginationMeta> {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = paymentRecordRepository.findAll(agent.id, farmerId, status, pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class GetPaymentRecordUseCase(
    private val paymentRecordRepository: PaymentRecordRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    fun execute(paymentId: UUID, userId: UUID): PaymentRecordDto {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val payment = paymentRecordRepository.findById(paymentId)
            ?: throw NotFoundException("Payment record not found")
        if (payment.agentId != agent.id) throw ForbiddenException("Access denied")
        return payment.toDto()
    }
}
