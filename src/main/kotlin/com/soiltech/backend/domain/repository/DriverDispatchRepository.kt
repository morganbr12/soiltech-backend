package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.DriverDispatch
import com.soiltech.backend.domain.enum.DispatchStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface DriverDispatchRepository {
    fun save(dispatch: DriverDispatch): DriverDispatch
    fun findById(id: UUID): DriverDispatch?
    fun findByOrderId(orderId: UUID): DriverDispatch?
    fun findAll(status: DispatchStatus?, pageable: Pageable): Page<DriverDispatch>
    fun updateStatus(id: UUID, status: DispatchStatus): DriverDispatch
}
