package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.DriverDispatch
import com.soiltech.backend.domain.enum.DispatchStatus
import com.soiltech.backend.domain.repository.DriverDispatchRepository
import com.soiltech.backend.infrastructure.persistence.entity.DriverDispatchJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.DriverDispatchJpaRepository
import com.soiltech.backend.interfaces.exception.NotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DriverDispatchRepositoryAdapter(
    private val jpaRepository: DriverDispatchJpaRepository
) : DriverDispatchRepository {

    override fun save(dispatch: DriverDispatch): DriverDispatch =
        jpaRepository.save(DriverDispatchJpaEntity.fromDomain(dispatch)).toDomain()

    override fun findById(id: UUID): DriverDispatch? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByOrderId(orderId: UUID): DriverDispatch? =
        jpaRepository.findByOrderId(orderId)?.toDomain()

    override fun findAll(status: DispatchStatus?, pageable: Pageable): Page<DriverDispatch> =
        jpaRepository.findAllFiltered(status, pageable).map { it.toDomain() }

    override fun updateStatus(id: UUID, status: DispatchStatus): DriverDispatch {
        val entity = jpaRepository.findById(id).orElseThrow { NotFoundException("Dispatch not found") }
        entity.status = status
        return jpaRepository.save(entity).toDomain()
    }
}
