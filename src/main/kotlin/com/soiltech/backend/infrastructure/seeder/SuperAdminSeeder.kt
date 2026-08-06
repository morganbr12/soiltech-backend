package com.soiltech.backend.infrastructure.seeder

import com.soiltech.backend.domain.entity.AdminProfile
import com.soiltech.backend.domain.entity.User
import com.soiltech.backend.domain.enum.AdminRoleName
import com.soiltech.backend.domain.enum.UserRole
import com.soiltech.backend.domain.repository.AdminProfileRepository
import com.soiltech.backend.domain.repository.AdminRoleRepository
import com.soiltech.backend.domain.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Order(2)
@Component
class SuperAdminSeeder(
    private val userRepository: UserRepository,
    private val adminProfileRepository: AdminProfileRepository,
    private val adminRoleRepository: AdminRoleRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${SUPER_ADMIN_EMAIL:}") private val email: String,
    @Value("\${SUPER_ADMIN_PASSWORD:}") private val password: String,
    @Value("\${SUPER_ADMIN_PHONE:}") private val phone: String,
    @Value("\${SUPER_ADMIN_FULL_NAME:Super Admin}") private val fullName: String,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(SuperAdminSeeder::class.java)

    @Transactional
    override fun run(args: ApplicationArguments) {
        if (email.isBlank() || password.isBlank() || phone.isBlank()) {
            log.warn("SuperAdminSeeder skipped — SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD, and SUPER_ADMIN_PHONE must all be set")
            return
        }

        if (userRepository.existsByEmail(email)) {
            log.info("Super admin already exists — skipping seed")
            return
        }

        val role = adminRoleRepository.findByName(AdminRoleName.SUPER_ADMIN)
            ?: run {
                log.error("SUPER_ADMIN role not found — ensure AdminRbacSeeder ran first")
                return
            }

        val now = LocalDateTime.now()
        val userId = UUID.randomUUID()

        userRepository.save(
            User(
                id = userId,
                email = email,
                phone = phone,
                passwordHash = passwordEncoder.encode(password),
                role = UserRole.ADMIN,
                isActive = true,
                lastLoginAt = null,
                createdAt = now,
                updatedAt = now
            )
        )

        adminProfileRepository.save(
            AdminProfile(
                id = UUID.randomUUID(),
                userId = userId,
                fullName = fullName,
                email = email,
                phone = phone,
                region = null,
                lbcId = null,
                adminRoleId = role.id,
                adminRoleName = AdminRoleName.SUPER_ADMIN,
                permissions = role.permissions,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )

        log.info("Super admin created — email: $email")
    }
}
