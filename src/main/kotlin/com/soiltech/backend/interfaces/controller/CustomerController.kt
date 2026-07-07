package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.customer.CustomerProfileDto
import com.soiltech.backend.application.dto.customer.UpdateCustomerProfileRequest
import com.soiltech.backend.application.usecase.customer.GetCustomerProfileUseCase
import com.soiltech.backend.application.usecase.customer.UpdateCustomerProfileUseCase
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/customer/profile")
@PreAuthorize("hasRole('CUSTOMER')")
class CustomerController(
    private val getCustomerProfileUseCase: GetCustomerProfileUseCase,
    private val updateCustomerProfileUseCase: UpdateCustomerProfileUseCase
) {

    @GetMapping
    fun getProfile(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<CustomerProfileDto>> {
        val data = getCustomerProfileUseCase.execute(principal.id)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @PutMapping
    fun updateProfile(
        @Valid @RequestBody request: UpdateCustomerProfileRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<CustomerProfileDto>> {
        val data = updateCustomerProfileUseCase.execute(principal.id, request)
        return ResponseEntity.ok(ApiResponse.success(data, "Profile updated"))
    }
}
