package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.admin.AdminProfileDto
import com.soiltech.backend.application.dto.admin.AdminRoleDto
import com.soiltech.backend.application.dto.admin.AssignAdminRoleRequest
import com.soiltech.backend.application.dto.admin.CreateAdminRequest
import com.soiltech.backend.application.usecase.admin.AssignAdminRoleUseCase
import com.soiltech.backend.application.usecase.admin.CreateAdminUserUseCase
import com.soiltech.backend.application.usecase.admin.GetAdminRoleUseCase
import com.soiltech.backend.application.usecase.admin.ListAdminRolesUseCase
import com.soiltech.backend.application.usecase.admin.ListAdminUsersUseCase
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/admin")
class AdminController(
    private val createAdminUserUseCase: CreateAdminUserUseCase,
    private val listAdminRolesUseCase: ListAdminRolesUseCase,
    private val getAdminRoleUseCase: GetAdminRoleUseCase,
    private val listAdminUsersUseCase: ListAdminUsersUseCase,
    private val assignAdminRoleUseCase: AssignAdminRoleUseCase
) {

    // ── Roles ───────────────────────────────────────────────────────────────

    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('settings:view')")
    fun listRoles(): ResponseEntity<ApiResponse<List<AdminRoleDto>>> =
        ResponseEntity.ok(ApiResponse.success(listAdminRolesUseCase.execute()))

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('settings:view')")
    fun getRole(@PathVariable id: UUID): ResponseEntity<ApiResponse<AdminRoleDto>> =
        ResponseEntity.ok(ApiResponse.success(getAdminRoleUseCase.execute(id)))

    // ── Admin users ─────────────────────────────────────────────────────────

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('users:view')")
    fun listAdminUsers(): ResponseEntity<ApiResponse<List<AdminProfileDto>>> =
        ResponseEntity.ok(ApiResponse.success(listAdminUsersUseCase.execute()))

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('users:create')")
    fun createAdmin(
        @Valid @RequestBody request: CreateAdminRequest
    ): ResponseEntity<ApiResponse<AdminProfileDto>> {
        val data = createAdminUserUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Admin user created successfully"))
    }

    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('roles:manage')")
    fun assignRole(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: AssignAdminRoleRequest
    ): ResponseEntity<ApiResponse<AdminProfileDto>> {
        val data = assignAdminRoleUseCase.execute(userId, request)
        return ResponseEntity.ok(ApiResponse.success(data, "Role assigned successfully"))
    }
}
