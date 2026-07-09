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
                lastLoginAt = null,
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
                phone = request.phone,
                region = request.region,
                lbcId = request.lbcId,
                adminRoleId = role.id,
                adminRoleName = role.name,
                permissions = role.permissions,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )

        return profile.toDto(phone = request.phone, lastLoginAt = null, roleDto = role.toDto())
    }
}

@Service
class GetAdminProfileUseCase(
    private val adminProfileRepository: AdminProfileRepository,
    private val userRepository: UserRepository
) {
    fun execute(userId: UUID): AdminProfileDto {
        val profile = adminProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Admin profile not found")
        val user = userRepository.findById(userId)
        return profile.toDto(
            phone = profile.phone ?: user?.phone ?: "",
            lastLoginAt = user?.lastLoginAt,
            roleDto = AdminRoleDto(
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
    private val adminProfileRepository: AdminProfileRepository,
    private val userRepository: UserRepository
) {
    fun execute(
        role: com.soiltech.backend.domain.enum.AdminRoleName?,
        isActive: Boolean?,
        search: String?,
        page: Int,
        perPage: Int
    ): Pair<List<AdminProfileDto>, com.soiltech.backend.interfaces.response.PaginationMeta> {
        val pageable = org.springframework.data.domain.PageRequest.of(
            page - 1, perPage, org.springframework.data.domain.Sort.by("createdAt").descending()
        )
        val result = adminProfileRepository.findAllFiltered(role, isActive, search, pageable)
        val userMap = result.content.mapNotNull { userRepository.findById(it.userId) }.associateBy { it.id }
        val dtos = result.content.map { profile ->
            val user = userMap[profile.userId]
            profile.toDto(
                phone = profile.phone ?: user?.phone ?: "",
                lastLoginAt = user?.lastLoginAt,
                roleDto = AdminRoleDto(
                    id = profile.adminRoleId,
                    name = profile.adminRoleName,
                    label = profile.adminRoleName.label,
                    permissions = profile.permissions,
                    permissionCount = profile.permissions.size
                )
            )
        }
        return dtos to com.soiltech.backend.interfaces.response.PaginationMeta.from(result, page, perPage)
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

        val lastLoginAt = userRepository.findById(userId)?.lastLoginAt
        return updated.toDto(phone = updated.phone ?: "", lastLoginAt = lastLoginAt, roleDto = role.toDto())
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

private fun AdminProfile.toDto(phone: String, lastLoginAt: java.time.LocalDateTime?, roleDto: AdminRoleDto) = AdminProfileDto(
    id = id,
    userId = userId,
    firstName = fullName.substringBefore(" ").ifBlank { fullName },
    lastName = fullName.substringAfter(" ", ""),
    fullName = fullName,
    email = email,
    phone = phone,
    region = region,
    lbcId = lbcId,
    role = roleDto,
    status = if (isActive) "active" else "inactive",
    lastLoginAt = lastLoginAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)
