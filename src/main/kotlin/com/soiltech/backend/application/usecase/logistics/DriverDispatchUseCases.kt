package com.soiltech.backend.application.usecase.logistics

import com.soiltech.backend.application.dto.logistics.DispatchDriverRequest
import com.soiltech.backend.application.dto.logistics.DriverDispatchDto
import com.soiltech.backend.application.dto.logistics.UpdateDispatchStatusRequest
import com.soiltech.backend.application.dto.order.CustomerOrderDto
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.domain.entity.DriverDispatch
import com.soiltech.backend.domain.entity.OrderTimeline
import com.soiltech.backend.domain.enum.DispatchStatus
import com.soiltech.backend.domain.enum.NotificationType
import com.soiltech.backend.domain.enum.OrderStatus
import com.soiltech.backend.domain.enum.ProduceOrderStatus
import com.soiltech.backend.domain.repository.CustomerOrderRepository
import com.soiltech.backend.domain.repository.CustomerProduceOrderRepository
import com.soiltech.backend.domain.repository.DriverDispatchRepository
import com.soiltech.backend.domain.repository.VehicleRepository
import com.soiltech.backend.infrastructure.service.NotificationService
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.ConflictException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

fun DriverDispatch.toDto() = DriverDispatchDto(
    id = id,
    orderId = orderId,
    customerName = customerName,
    vehicleId = vehicleId,
    driverName = driverName,
    plateNumber = plateNumber,
    vehicleType = vehicleType,
    scheduledDate = scheduledDate,
    pickupLocation = pickupLocation,
    notes = notes,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

@Service
class AgentFieldConfirmUseCase(
    private val customerOrderRepository: CustomerOrderRepository,
    private val notificationService: NotificationService
) {
    @Transactional
    fun execute(orderId: UUID, agentId: UUID): CustomerOrderDto {
        val order = customerOrderRepository.findById(orderId)
            ?: throw NotFoundException("Order not found")

        if (order.status != OrderStatus.CONFIRMED) {
            throw BadRequestException("Order must be in CONFIRMED status before field confirmation. Current status: ${order.status.value}")
        }

        customerOrderRepository.updateStatus(orderId, OrderStatus.AGENT_CONFIRMED)

        customerOrderRepository.saveTimeline(
            OrderTimeline(
                id = UUID.randomUUID(),
                orderId = orderId,
                status = OrderStatus.AGENT_CONFIRMED,
                note = "Agent confirmed produce is ready at the farm",
                createdAt = LocalDateTime.now(),
                createdBy = agentId
            )
        )

        notificationService.pushToCustomer(
            customerProfileId = order.customerId,
            title = "Order Ready for Pickup",
            body = "Your order has been verified in the field and is ready for pickup.",
            type = NotificationType.ORDER_AGENT_CONFIRMED,
            referenceId = orderId,
            referenceType = "ORDER"
        )

        val updated = customerOrderRepository.findById(orderId)!!
        val items = customerOrderRepository.findItemsByOrderId(orderId)
        val timeline = customerOrderRepository.findTimelineByOrderId(orderId)
        return updated.toDto(items, timeline)
    }
}

@Service
class AdminDispatchDriverUseCase(
    private val produceOrderRepository: CustomerProduceOrderRepository,
    private val vehicleRepository: VehicleRepository,
    private val driverDispatchRepository: DriverDispatchRepository,
    private val notificationService: NotificationService
) {
    @Transactional
    fun execute(orderId: UUID, request: DispatchDriverRequest, adminId: UUID): DriverDispatchDto {
        val order = produceOrderRepository.findById(orderId)
            ?: throw NotFoundException("Order not found")

        if (order.status != ProduceOrderStatus.CONFIRMED) {
            throw BadRequestException("Order must be CONFIRMED before dispatching a driver. Current status: ${order.status.value}")
        }

        if (driverDispatchRepository.findByOrderId(orderId) != null) {
            throw ConflictException("A driver has already been dispatched for this order")
        }

        val vehicle = vehicleRepository.findById(request.vehicleId)
            ?: throw NotFoundException("Vehicle not found")

        if (vehicle.status == com.soiltech.backend.domain.enum.VehicleStatus.MAINTENANCE ||
            vehicle.status == com.soiltech.backend.domain.enum.VehicleStatus.INACTIVE
        ) {
            throw BadRequestException("Vehicle '${vehicle.carPlateNumber}' is not available for dispatch (status: ${vehicle.status})")
        }

        val dispatch = driverDispatchRepository.save(
            DriverDispatch(
                id = UUID.randomUUID(),
                orderId = orderId,
                customerName = order.customerName,
                vehicleId = vehicle.id,
                driverName = vehicle.driverName,
                plateNumber = vehicle.carPlateNumber,
                vehicleType = vehicle.vehicleType,
                scheduledDate = request.scheduledDate,
                pickupLocation = request.pickupLocation ?: order.region,
                notes = request.notes,
                status = DispatchStatus.PENDING,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                createdBy = adminId,
                updatedBy = null
            )
        )

        vehicleRepository.save(vehicle.copy(status = com.soiltech.backend.domain.enum.VehicleStatus.ON_ROUTE, updatedAt = LocalDateTime.now()))

        produceOrderRepository.save(
            order.copy(
                status = ProduceOrderStatus.PROCESSING,
                assignedDriver = "${vehicle.driverName} (${vehicle.carPlateNumber})",
                updatedAt = LocalDateTime.now()
            )
        )

        notificationService.pushToCustomer(
            customerProfileId = order.customerId,
            title = "Driver Dispatched",
            body = "A driver (${vehicle.driverName}, ${vehicle.carPlateNumber}) has been dispatched for your order.",
            type = NotificationType.ORDER_DRIVER_DISPATCHED,
            referenceId = orderId,
            referenceType = "ORDER"
        )

        return dispatch.toDto()
    }
}

@Service
class ListDriverDispatchesUseCase(
    private val driverDispatchRepository: DriverDispatchRepository
) {
    fun execute(status: DispatchStatus?, page: Int, perPage: Int): Pair<List<DriverDispatchDto>, PaginationMeta> {
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = driverDispatchRepository.findAll(status, pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class UpdateDispatchStatusUseCase(
    private val driverDispatchRepository: DriverDispatchRepository,
    private val produceOrderRepository: CustomerProduceOrderRepository,
    private val vehicleRepository: VehicleRepository,
    private val notificationService: NotificationService
) {
    @Transactional
    fun execute(dispatchId: UUID, request: UpdateDispatchStatusRequest, updatedBy: UUID): DriverDispatchDto {
        val dispatch = driverDispatchRepository.findById(dispatchId)
            ?: throw NotFoundException("Dispatch not found")
        val order = produceOrderRepository.findById(dispatch.orderId)

        val updated = driverDispatchRepository.updateStatus(dispatchId, request.status)

        when (request.status) {
            DispatchStatus.EN_ROUTE, DispatchStatus.PICKED_UP -> {
                order?.let {
                    produceOrderRepository.save(it.copy(status = ProduceOrderStatus.PROCESSING, updatedAt = LocalDateTime.now()))
                    notificationService.pushToCustomer(
                        customerProfileId = it.customerId,
                        title = "Order On the Way",
                        body = "Your order is on the way! Driver ${dispatch.driverName} (${dispatch.plateNumber}) is heading to you.",
                        type = NotificationType.ORDER_SHIPPED,
                        referenceId = dispatch.orderId,
                        referenceType = "ORDER"
                    )
                }
            }
            DispatchStatus.DELIVERED -> {
                order?.let {
                    produceOrderRepository.save(it.copy(
                        status = ProduceOrderStatus.DELIVERED,
                        deliveryDate = java.time.LocalDate.now(),
                        updatedAt = LocalDateTime.now()
                    ))
                    notificationService.pushToCustomer(
                        customerProfileId = it.customerId,
                        title = "Order Delivered",
                        body = "Your order has been delivered. Thank you for choosing SoilTech!",
                        type = NotificationType.ORDER_DELIVERED,
                        referenceId = dispatch.orderId,
                        referenceType = "ORDER"
                    )
                }
                vehicleRepository.findById(dispatch.vehicleId)?.let { vehicle ->
                    vehicleRepository.save(vehicle.copy(status = com.soiltech.backend.domain.enum.VehicleStatus.AVAILABLE, updatedAt = LocalDateTime.now()))
                }
            }
            DispatchStatus.CANCELLED -> {
                vehicleRepository.findById(dispatch.vehicleId)?.let { vehicle ->
                    vehicleRepository.save(vehicle.copy(status = com.soiltech.backend.domain.enum.VehicleStatus.AVAILABLE, updatedAt = LocalDateTime.now()))
                }
            }
            else -> {}
        }

        return updated.toDto()
    }
}
