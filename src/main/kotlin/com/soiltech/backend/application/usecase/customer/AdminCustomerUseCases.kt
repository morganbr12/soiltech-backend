package com.soiltech.backend.application.usecase.customer

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.domain.entity.CustomerProfile
import com.soiltech.backend.domain.entity.CustomerWallet
import com.soiltech.backend.domain.entity.User
import com.soiltech.backend.domain.enum.*
import com.soiltech.backend.domain.repository.*
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.ConflictException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Year
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

// ── List ──────────────────────────────────────────────────────────────────────

@Service
class ListCustomersUseCase(
    private val customerProfileRepository: CustomerProfileRepository,
    private val kycDocumentRepository: KycDocumentRepository
) {
    fun execute(
        status: CustomerStatus?,
        tier: CustomerTier?,
        region: String?,
        search: String?,
        page: Int,
        limit: Int,
        sortBy: String,
        sortOrder: String
    ): Triple<List<AdminCustomerResponse>, AdminCustomerSummaryResponse, PaginationMeta> {
        val direction = if (sortOrder.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val sortField = SORTABLE_FIELDS.getOrDefault(sortBy, "createdAt")
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), limit.coerceIn(1, 100), Sort.by(direction, sortField))

        val resultPage = customerProfileRepository.findAll(status, tier, region, search, pageable)
        val statusCounts = customerProfileRepository.countByStatus()

        val ids = resultPage.content.map { it.id }
        val metricsMap = customerProfileRepository.findMetricsByCustomerIds(ids)

        val items = resultPage.content.map { profile ->
            val metrics = metricsMap[profile.id] ?: com.soiltech.backend.domain.entity.CustomerMetrics()
            profile.toAdminResponse(metrics)
        }

        val summary = AdminCustomerSummaryResponse(
            total = statusCounts.values.sumOf { it },
            active = statusCounts.getOrDefault(CustomerStatus.ACTIVE, 0L),
            verified = statusCounts.getOrDefault(CustomerStatus.VERIFIED, 0L),
            pending = statusCounts.getOrDefault(CustomerStatus.PENDING, 0L),
            suspended = statusCounts.getOrDefault(CustomerStatus.SUSPENDED, 0L),
            rejected = statusCounts.getOrDefault(CustomerStatus.REJECTED, 0L)
        )
        return Triple(items, summary, PaginationMeta.from(resultPage, page, limit))
    }

    companion object {
        val SORTABLE_FIELDS = mapOf(
            "totalSpent" to "createdAt",
            "totalOrders" to "createdAt",
            "joinedDate" to "joinedDate",
            "rating" to "rating",
            "createdAt" to "createdAt"
        )
    }
}

// ── Get single ────────────────────────────────────────────────────────────────

@Service
class GetCustomerAdminUseCase(
    private val customerProfileRepository: CustomerProfileRepository,
    private val kycDocumentRepository: KycDocumentRepository
) {
    fun execute(id: UUID): AdminCustomerResponse {
        val profile = customerProfileRepository.findById(id)
            ?: throw NotFoundException("Customer not found with id: $id")
        val metrics = customerProfileRepository.findMetricsByCustomerIds(listOf(id))[id]
            ?: com.soiltech.backend.domain.entity.CustomerMetrics()
        val docs = kycDocumentRepository.findByCustomerId(id).map {
            KycDocumentDto(type = it.type, url = it.url, uploadedAt = it.uploadedAt)
        }
        return profile.toAdminResponse(metrics, docs)
    }
}

// ── Create ────────────────────────────────────────────────────────────────────

@Service
class CreateCustomerUseCase(
    private val customerProfileRepository: CustomerProfileRepository,
    private val customerWalletRepository: CustomerWalletRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun execute(request: CreateCustomerRequest): AdminCustomerResponse {
        if (userRepository.existsByEmail(request.email))
            throw ConflictException("A user with email '${request.email}' already exists")
        if (customerProfileRepository.existsByPhone(request.phone))
            throw ConflictException("A customer with phone '${request.phone}' already exists")
        request.nationalId?.let {
            if (customerProfileRepository.existsByNationalId(it))
                throw ConflictException("A customer with national ID '$it' already exists")
        }

        val now = LocalDateTime.now()
        val customerCode = generateUniqueCode(customerProfileRepository)
        val fullName = "${request.firstName} ${request.lastName}"

        val user = userRepository.save(
            User(
                id = UUID.randomUUID(),
                email = request.email,
                phone = request.phone,
                passwordHash = passwordEncoder.encode(request.password),
                role = UserRole.CUSTOMER,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )

        val profile = customerProfileRepository.save(
            CustomerProfile(
                id = UUID.randomUUID(),
                userId = user.id,
                customerCode = customerCode,
                firstName = request.firstName,
                lastName = request.lastName,
                fullName = fullName,
                email = request.email,
                phone = request.phone,
                region = request.region,
                district = request.district,
                address = request.address,
                profileImageUrl = null,
                accountType = request.accountType,
                location = request.region,
                status = CustomerStatus.PENDING,
                tier = CustomerTier.BRONZE,
                businessName = request.businessName,
                businessType = request.businessType,
                nationalId = request.nationalId,
                isVerified = false,
                verifiedDate = null,
                rejectionReason = null,
                rating = 0.0,
                lat = null,
                lng = null,
                joinedDate = now,
                createdAt = now,
                updatedAt = now
            )
        )

        customerWalletRepository.save(
            CustomerWallet(
                id = UUID.randomUUID(),
                customerId = profile.id,
                customerCode = customerCode,
                customerName = fullName,
                balance = BigDecimal.ZERO,
                pendingAmount = BigDecimal.ZERO,
                totalDeposited = BigDecimal.ZERO,
                totalWithdrawn = BigDecimal.ZERO,
                lastTransaction = null,
                lastTransactionDate = null,
                status = WalletStatus.ACTIVE,
                region = request.region,
                createdAt = now,
                updatedAt = now
            )
        )

        return profile.toAdminResponse(com.soiltech.backend.domain.entity.CustomerMetrics())
    }

    private fun generateUniqueCode(repo: CustomerProfileRepository): String {
        val count = repo.countAll()
        var num = (count + 1).toInt()
        var code: String
        do { code = "CUST-${String.format("%04d", num++)}" } while (repo.existsByCustomerCode(code))
        return code
    }
}

// ── Update ────────────────────────────────────────────────────────────────────

@Service
class UpdateCustomerAdminUseCase(private val customerProfileRepository: CustomerProfileRepository) {
    @Transactional
    fun execute(id: UUID, request: UpdateCustomerAdminRequest): AdminCustomerResponse {
        val existing = customerProfileRepository.findById(id)
            ?: throw NotFoundException("Customer not found with id: $id")

        request.email?.let { if (customerProfileRepository.existsByEmailAndIdNot(it, id)) throw ConflictException("Email '$it' already in use") }
        request.phone?.let { if (customerProfileRepository.existsByPhoneAndIdNot(it, id)) throw ConflictException("Phone '$it' already in use") }
        request.nationalId?.let { if (customerProfileRepository.existsByNationalIdAndIdNot(it, id)) throw ConflictException("National ID '$it' already in use") }

        val firstName = request.firstName ?: existing.firstName
        val lastName = request.lastName ?: existing.lastName
        val fullName = if (firstName != null && lastName != null) "$firstName $lastName" else existing.fullName

        val updated = existing.copy(
            firstName = firstName,
            lastName = lastName,
            fullName = fullName,
            email = request.email ?: existing.email,
            phone = request.phone ?: existing.phone,
            region = request.region ?: existing.region,
            district = request.district ?: existing.district,
            address = request.address ?: existing.address,
            businessName = request.businessName ?: existing.businessName,
            businessType = request.businessType ?: existing.businessType,
            nationalId = request.nationalId ?: existing.nationalId,
            accountType = request.accountType ?: existing.accountType,
            tier = request.tier ?: existing.tier,
            rating = request.rating ?: existing.rating,
            lat = request.lat ?: existing.lat,
            lng = request.lng ?: existing.lng,
            updatedAt = LocalDateTime.now()
        )
        val saved = customerProfileRepository.update(updated)
        val metrics = customerProfileRepository.findMetricsByCustomerIds(listOf(id))[id]
            ?: com.soiltech.backend.domain.entity.CustomerMetrics()
        return saved.toAdminResponse(metrics)
    }
}

// ── Delete ────────────────────────────────────────────────────────────────────

@Service
class DeleteCustomerUseCase(
    private val customerProfileRepository: CustomerProfileRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun execute(id: UUID) {
        val profile = customerProfileRepository.findById(id)
            ?: throw NotFoundException("Customer not found with id: $id")
        customerProfileRepository.delete(id)
        userRepository.delete(profile.userId)
    }
}

// ── Verify (KYC approve) ──────────────────────────────────────────────────────

@Service
class VerifyCustomerUseCase(private val customerProfileRepository: CustomerProfileRepository) {
    @Transactional
    fun execute(id: UUID): AdminCustomerResponse {
        val profile = customerProfileRepository.findById(id)
            ?: throw NotFoundException("Customer not found with id: $id")
        if (profile.status != CustomerStatus.PENDING)
            throw BadRequestException("Only PENDING customers can be verified. Current status: ${profile.status.value}")
        val now = LocalDateTime.now()
        val updated = profile.copy(status = CustomerStatus.VERIFIED, isVerified = true, verifiedDate = now, rejectionReason = null, updatedAt = now)
        val saved = customerProfileRepository.update(updated)
        val metrics = customerProfileRepository.findMetricsByCustomerIds(listOf(id))[id]
            ?: com.soiltech.backend.domain.entity.CustomerMetrics()
        return saved.toAdminResponse(metrics)
    }
}

// ── Reject (KYC reject) ───────────────────────────────────────────────────────

@Service
class RejectCustomerUseCase(private val customerProfileRepository: CustomerProfileRepository) {
    @Transactional
    fun execute(id: UUID, request: RejectCustomerRequest): AdminCustomerResponse {
        val profile = customerProfileRepository.findById(id)
            ?: throw NotFoundException("Customer not found with id: $id")
        if (profile.status != CustomerStatus.PENDING)
            throw BadRequestException("Only PENDING customers can be rejected. Current status: ${profile.status.value}")
        val updated = profile.copy(status = CustomerStatus.REJECTED, rejectionReason = request.reason, updatedAt = LocalDateTime.now())
        val saved = customerProfileRepository.update(updated)
        val metrics = customerProfileRepository.findMetricsByCustomerIds(listOf(id))[id]
            ?: com.soiltech.backend.domain.entity.CustomerMetrics()
        return saved.toAdminResponse(metrics)
    }
}

// ── Suspend ───────────────────────────────────────────────────────────────────

@Service
class SuspendCustomerUseCase(private val customerProfileRepository: CustomerProfileRepository) {
    @Transactional
    fun execute(id: UUID, request: SuspendCustomerRequest): AdminCustomerResponse {
        val profile = customerProfileRepository.findById(id)
            ?: throw NotFoundException("Customer not found with id: $id")
        if (profile.status !in listOf(CustomerStatus.ACTIVE, CustomerStatus.VERIFIED))
            throw BadRequestException("Only ACTIVE or VERIFIED customers can be suspended. Current status: ${profile.status.value}")
        val updated = profile.copy(status = CustomerStatus.SUSPENDED, rejectionReason = request.reason, updatedAt = LocalDateTime.now())
        val saved = customerProfileRepository.update(updated)
        val metrics = customerProfileRepository.findMetricsByCustomerIds(listOf(id))[id]
            ?: com.soiltech.backend.domain.entity.CustomerMetrics()
        return saved.toAdminResponse(metrics)
    }
}

// ── Activate ──────────────────────────────────────────────────────────────────

@Service
class ActivateCustomerUseCase(private val customerProfileRepository: CustomerProfileRepository) {
    @Transactional
    fun execute(id: UUID): AdminCustomerResponse {
        val profile = customerProfileRepository.findById(id)
            ?: throw NotFoundException("Customer not found with id: $id")
        if (profile.status != CustomerStatus.SUSPENDED)
            throw BadRequestException("Only SUSPENDED customers can be activated. Current status: ${profile.status.value}")
        val updated = profile.copy(status = CustomerStatus.ACTIVE, rejectionReason = null, updatedAt = LocalDateTime.now())
        val saved = customerProfileRepository.update(updated)
        val metrics = customerProfileRepository.findMetricsByCustomerIds(listOf(id))[id]
            ?: com.soiltech.backend.domain.entity.CustomerMetrics()
        return saved.toAdminResponse(metrics)
    }
}

// ── Dashboard ─────────────────────────────────────────────────────────────────

@Service
class CustomerDashboardUseCase(
    private val customerProfileRepository: CustomerProfileRepository,
    private val customerOrderRepository: com.soiltech.backend.domain.repository.CustomerOrderRepository
) {
    fun execute(): CustomerDashboardResponse {
        val statusCounts = customerProfileRepository.countByStatus()
        val tierCounts = customerProfileRepository.countByTier()
        val totalOrders = customerOrderRepository.countAll()
        val totalRevenue = customerOrderRepository.sumTotalAmount()
        val avgRating = customerProfileRepository.avgRating()

        val year = Year.now().value

        val topSpenders = customerOrderRepository.findTopSpenders(7)
        val topCustomers = topSpenders.mapNotNull { (customerId, orderCount, totalSpent) ->
            val profile = customerProfileRepository.findById(customerId) ?: return@mapNotNull null
            TopCustomerDto(
                id = profile.id,
                customerCode = profile.customerCode ?: "",
                fullName = profile.fullName,
                region = profile.region,
                totalOrders = orderCount,
                totalSpent = totalSpent
            )
        }

        val monthlyGrowth = customerProfileRepository.countMonthlyNewCustomers(year)
        val monthlyRevenue = customerOrderRepository.sumMonthlyRevenue(year)

        val recentOrders = customerOrderRepository.findRecent(7).map { order ->
            val items = customerOrderRepository.findItemsByOrderId(order.id)
            val produce = items.firstOrNull()?.productName ?: "N/A"
            RecentOrderDto(
                id = order.id,
                orderCode = order.id.toString().take(8).uppercase(),
                customerId = order.customerId,
                customerName = order.customerName ?: "Unknown",
                produce = produce,
                totalAmount = order.totalAmount,
                status = order.status.value
            )
        }

        val activeCount = (statusCounts[CustomerStatus.ACTIVE] ?: 0L) + (statusCounts[CustomerStatus.VERIFIED] ?: 0L)

        return CustomerDashboardResponse(
            kpis = CustomerKpiDto(
                totalCustomers = statusCounts.values.sumOf { it },
                activeCustomers = activeCount,
                pendingVerification = statusCounts[CustomerStatus.PENDING] ?: 0L,
                totalRevenue = totalRevenue,
                avgRating = avgRating,
                totalOrders = totalOrders
            ),
            statusBreakdown = statusCounts.map { (k, v) -> k.value to v }.toMap(),
            tierBreakdown = tierCounts.map { (k, v) -> k.value to v }.toMap(),
            topCustomers = topCustomers,
            monthlyGrowth = monthlyGrowth,
            monthlyRevenue = monthlyRevenue,
            recentOrders = recentOrders
        )
    }
}

// ── Mapper ────────────────────────────────────────────────────────────────────

internal fun CustomerProfile.toAdminResponse(
    metrics: com.soiltech.backend.domain.entity.CustomerMetrics,
    kycDocs: List<KycDocumentDto> = emptyList()
) = AdminCustomerResponse(
    id = id,
    customerCode = customerCode ?: "",
    firstName = firstName ?: "",
    lastName = lastName ?: "",
    fullName = fullName,
    email = email,
    phone = phone,
    region = region,
    district = district,
    address = address,
    status = status,
    tier = tier,
    totalOrders = metrics.totalOrders,
    totalSpent = metrics.totalSpent,
    walletBalance = metrics.walletBalance,
    rating = rating,
    joinedDate = joinedDate,
    lastOrderDate = metrics.lastOrderDate,
    verifiedDate = verifiedDate,
    lat = lat,
    lng = lng,
    businessName = businessName,
    businessType = businessType,
    nationalId = nationalId,
    isVerified = isVerified,
    accountType = accountType,
    kycDocuments = kycDocs,
    createdAt = createdAt,
    updatedAt = updatedAt
)
