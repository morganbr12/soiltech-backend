package com.soiltech.backend.application.dto.admin

import com.soiltech.backend.domain.enum.AdminRoleName
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.UUID

data class AdminRoleDto(
    val id: UUID,
    val name: AdminRoleName,
    val label: String,
    val permissions: Set<String>,
    val permissionCount: Int
)

data class AdminProfileDto(
    val id: UUID,
    val userId: UUID,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val region: String?,
    val lbcId: UUID?,
    val role: AdminRoleDto,
    val status: String,
    val lastLoginAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreateAdminRequest(
    @field:NotBlank @field:Email
    val email: String,
    @field:NotBlank @field:Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number")
    val phone: String,
    @field:NotBlank
    val fullName: String,
    val region: String? = null,
    val lbcId: UUID? = null,
    @field:NotBlank @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
    @field:NotNull
    val adminRole: AdminRoleName
)

data class AssignAdminRoleRequest(
    @field:NotNull
    val adminRole: AdminRoleName
)
