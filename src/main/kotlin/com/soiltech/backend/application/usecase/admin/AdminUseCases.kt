package com.soiltech.backend.application.usecase.admin

import com.soiltech.backend.application.dto.admin.AdminProfileDto
import com.soiltech.backend.application.dto.admin.AdminRoleDto
import com.soiltech.backend.application.dto.admin.AssignAdminRoleRequest
import com.soiltech.backend.application.dto.admin.CreateAdminRequest
import com.soiltech.backend.domain.entity.AdminProfile
import com.soiltech.backend.domain.entity.User
import com.soiltech.backend.domain.enum.UserRole
import com.soiltech.backend.domain.repository.AdminProfileRepository
import com.soiltech.backend.domain.repository.AdminRoleRepository
import com.soiltech.backend.domain.repository.UserRepository
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.ConflictException
import com.soiltech.backend.interfaces.exception.NotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class CreateAdminUserUseCase(
    private val userRepository: UserRepository,
    private val adminProfileRepository: AdminProfileRepository,
    private val adminRoleRepository: AdminRoleRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun execute(request: CreateAdminRequest): AdminProfileDto {
        if (userRepository.existsByEmail(request.email)) throw ConflictException("Email already registered")
        if (userRepository.existsByPhone(request.phone)) throw ConflictException("Phone number already registered")

        val role = adminRoleRepository.findByName(request.adminRole)
            ?: throw BadRequestException("Admin role '${request.adminRole.value}' not found — ensure seeder has run")

        val now = LocalDateTime.now()
        val userId = UUID.randomUUID()

        val user = userRepository.save(
            User(
                id = userId,
                email = request.email,
                phone = request.phone,
                passwordHash = passwordEncoder.encode(request.password),
                role = UserRole.ADMIN,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )

        val profile = adminProfileRepository.save(
            AdminProfile(
                id = UUID.randomUUID(),
                userId = user.id,
                fullName = request.fullName,
                email = user.email,
                adminRoleId = role.id,
                adminRoleName = role.name,
                permissions = role.permissions,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )

        return profile.toDto(role.toDto())
    }
}

@Service
class ListAdminRolesUseCase(
    private val adminRoleRepository: AdminRoleRepository
) {
    fun execute(): List<AdminRoleDto> = adminRoleRepository.findAll().map { it.toDto() }
}

@Service
class GetAdminRoleUseCase(
    private val adminRoleRepository: AdminRoleRepository
) {
    fun execute(id: UUID): AdminRoleDto =
        adminRoleRepository.findById(id)?.toDto()
            ?: throw NotFoundException("Admin role not found")
}

@Service
class ListAdminUsersUseCase(
    private val adminProfileRepository: AdminProfileRepository
) {
    fun execute(): List<AdminProfileDto> = adminProfileRepository.findAll().map { profile ->
        profile.toDto(
            AdminRoleDto(
                id = profile.adminRoleId,
                name = profile.adminRoleName,
                label = profile.adminRoleName.label,
                permissions = profile.permissions,
                permissionCount = profile.permissions.size
            )
        )
    }
}

@Service
class AssignAdminRoleUseCase(
    private val adminProfileRepository: AdminProfileRepository,
    private val adminRoleRepository: AdminRoleRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun execute(userId: UUID, request: AssignAdminRoleRequest): AdminProfileDto {
        userRepository.findById(userId) ?: throw NotFoundException("User not found")

        val role = adminRoleRepository.findByName(request.adminRole)
            ?: throw BadRequestException("Admin role '${request.adminRole.value}' not found")

        val existing = adminProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Admin profile not found for user $userId")

        val updated = adminProfileRepository.save(
            existing.copy(
                adminRoleId = role.id,
                adminRoleName = role.name,
                permissions = role.permissions
            )
        )

        return updated.toDto(role.toDto())
    }
}

// Extension mappers scoped to admin use cases
private fun com.soiltech.backend.domain.entity.AdminRole.toDto() = AdminRoleDto(
    id = id,
    name = name,
    label = label,
    permissions = permissions,
    permissionCount = permissions.size
)

private fun AdminProfile.toDto(roleDto: AdminRoleDto) = AdminProfileDto(
    id = id,
    userId = userId,
    fullName = fullName,
    email = email,
    role = roleDto,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)
