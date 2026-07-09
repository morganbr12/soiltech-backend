package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.application.usecase.customer.*
import com.soiltech.backend.domain.enum.CustomerStatus
import com.soiltech.backend.domain.enum.CustomerTier
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/customers")
class AdminCustomerController(
    private val listCustomersUseCase: ListCustomersUseCase,
    private val getCustomerAdminUseCase: GetCustomerAdminUseCase,
    private val createCustomerUseCase: CreateCustomerUseCase,
    private val updateCustomerAdminUseCase: UpdateCustomerAdminUseCase,
    private val deleteCustomerUseCase: DeleteCustomerUseCase,
    private val verifyCustomerUseCase: VerifyCustomerUseCase,
    private val rejectCustomerUseCase: RejectCustomerUseCase,
    private val suspendCustomerUseCase: SuspendCustomerUseCase,
    private val activateCustomerUseCase: ActivateCustomerUseCase,
    private val customerDashboardUseCase: CustomerDashboardUseCase
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:view')")
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) tier: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(name = "sortBy", defaultValue = "createdAt") sortBy: String,
        @RequestParam(name = "sortOrder", defaultValue = "desc") sortOrder: String
    ): ResponseEntity<ApiResponse<List<AdminCustomerResponse>>> {
        val customerStatus = status?.let { CustomerStatus.fromValue(it) }
        val customerTier = tier?.let { CustomerTier.fromValue(it) }
        val (items, summary, meta) = listCustomersUseCase.execute(
            customerStatus, customerTier, region, search, page, limit, sortBy, sortOrder
        )
        return ResponseEntity.ok(ApiResponse.success(items, meta = meta, summary = summary))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:create')")
    fun create(
        @Valid @RequestBody request: CreateCustomerRequest
    ): ResponseEntity<ApiResponse<AdminCustomerResponse>> {
        val data = createCustomerUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Customer created successfully"))
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:analytics')")
    fun dashboard(): ResponseEntity<ApiResponse<CustomerDashboardResponse>> {
        val data = customerDashboardUseCase.execute()
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:analytics')")
    fun dashboardSummary(): ResponseEntity<ApiResponse<CustomerDashboardResponse>> {
        val data = customerDashboardUseCase.execute()
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:view')")
    fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponse<AdminCustomerResponse>> {
        val data = getCustomerAdminUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:edit')")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateCustomerAdminRequest
    ): ResponseEntity<ApiResponse<AdminCustomerResponse>> {
        val data = updateCustomerAdminUseCase.execute(id, request)
        return ResponseEntity.ok(ApiResponse.success(data, "Customer updated successfully"))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:delete')")
    fun delete(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit?>> {
        deleteCustomerUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(null, "Customer deleted successfully"))
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:verify')")
    fun verify(@PathVariable id: UUID): ResponseEntity<ApiResponse<AdminCustomerResponse>> {
        val data = verifyCustomerUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(data, "Customer verified successfully"))
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:verify')")
    fun reject(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: RejectCustomerRequest?
    ): ResponseEntity<ApiResponse<AdminCustomerResponse>> {
        val data = rejectCustomerUseCase.execute(id, request ?: RejectCustomerRequest())
        return ResponseEntity.ok(ApiResponse.success(data, "Customer rejected"))
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:suspend')")
    fun suspend(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: SuspendCustomerRequest?
    ): ResponseEntity<ApiResponse<AdminCustomerResponse>> {
        val data = suspendCustomerUseCase.execute(id, request ?: SuspendCustomerRequest())
        return ResponseEntity.ok(ApiResponse.success(data, "Customer suspended"))
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:suspend')")
    fun activate(@PathVariable id: UUID): ResponseEntity<ApiResponse<AdminCustomerResponse>> {
        val data = activateCustomerUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(data, "Customer activated successfully"))
    }
}
