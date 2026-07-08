package com.soiltech.backend.infrastructure.seeder

import com.soiltech.backend.domain.enum.AdminRoleName
import com.soiltech.backend.infrastructure.persistence.entity.AdminRoleJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.AdminRoleJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class AdminRbacSeeder(
    private val adminRoleJpaRepository: AdminRoleJpaRepository
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(AdminRbacSeeder::class.java)

    @Transactional
    override fun run(args: ApplicationArguments) {
        ROLE_PERMISSIONS.forEach { (roleName, permissions) ->
            val existing = adminRoleJpaRepository.findByName(roleName)
            if (existing == null) {
                adminRoleJpaRepository.save(
                    AdminRoleJpaEntity(
                        id = UUID.randomUUID(),
                        name = roleName,
                        label = roleName.label,
                        permissions = permissions.toMutableSet()
                    )
                )
                log.info("Seeded admin role: ${roleName.value}")
            } else {
                existing.permissions.clear()
                existing.permissions.addAll(permissions)
                adminRoleJpaRepository.save(existing)
            }
        }
        log.info("RBAC seeding complete — ${ROLE_PERMISSIONS.size} roles, ${ALL_PERMISSIONS.size} permissions")
    }

    companion object {
        val ALL_PERMISSIONS: Set<String> = setOf(
            "dashboard:view",
            "users:view", "users:create", "users:edit", "users:delete",
            "lbc:view", "lbc:create", "lbc:edit", "lbc:delete", "lbc:suspend",
            "agents:view", "agents:create", "agents:edit", "agents:delete", "agents:transfer", "agents:track",
            "farmers:view", "farmers:create", "farmers:approve", "farmers:edit",
            "farms:view", "farms:create", "farms:edit",
            "produce:view", "produce:manage", "produce:approve",
            "warehouses:view", "warehouses:manage", "warehouses:inventory",
            "logistics:view", "logistics:manage", "logistics:dispatch",
            "tracking:view", "tracking:realtime",
            "payments:view", "payments:approve", "payments:process", "payments:refund",
            "reports:view", "reports:export",
            "analytics:view", "analytics:advanced",
            "notifications:view", "notifications:send",
            "audit:view",
            "settings:view", "settings:manage", "roles:manage",
            "customers:view", "customers:create", "customers:edit", "customers:delete",
            "customers:verify", "customers:orders", "customers:wallet",
            "customers:notifications", "customers:analytics", "customers:reports"
        )

        val ROLE_PERMISSIONS: Map<AdminRoleName, Set<String>> = mapOf(
            AdminRoleName.SUPER_ADMIN to ALL_PERMISSIONS,
            AdminRoleName.OPERATIONS_MANAGER to setOf(
                "dashboard:view",
                "users:view",
                "lbc:view", "lbc:edit",
                "agents:view", "agents:create", "agents:edit", "agents:delete", "agents:transfer",
                "farmers:view", "farmers:approve", "farmers:edit",
                "farms:view", "farms:edit",
                "produce:view", "produce:manage", "produce:approve",
                "warehouses:view",
                "logistics:view", "logistics:manage",
                "tracking:view", "tracking:realtime",
                "payments:view", "payments:approve",
                "reports:view", "reports:export",
                "analytics:view",
                "notifications:view",
                "audit:view",
                "settings:view",
                "customers:view", "customers:create", "customers:edit", "customers:verify",
                "customers:orders", "customers:wallet", "customers:notifications",
                "customers:analytics", "customers:reports"
            ),
            AdminRoleName.REGIONAL_MANAGER to setOf(
                "dashboard:view",
                "agents:view", "agents:edit", "agents:track",
                "farmers:view", "farmers:approve",
                "farms:view",
                "produce:view", "produce:manage",
                "logistics:view",
                "tracking:view", "tracking:realtime",
                "reports:view",
                "analytics:view",
                "notifications:view",
                "customers:view", "customers:orders", "customers:analytics", "customers:reports"
            ),
            AdminRoleName.LBC_MANAGER to setOf(
                "dashboard:view",
                "lbc:view",
                "agents:view", "agents:create", "agents:edit",
                "farmers:view", "farmers:create",
                "farms:view",
                "produce:view",
                "tracking:view",
                "reports:view",
                "notifications:view"
            ),
            AdminRoleName.FINANCE_MANAGER to setOf(
                "dashboard:view",
                "payments:view", "payments:approve", "payments:process", "payments:refund",
                "reports:view", "reports:export",
                "analytics:view", "analytics:advanced",
                "audit:view",
                "notifications:view",
                "customers:view", "customers:wallet", "customers:orders",
                "customers:analytics", "customers:reports"
            ),
            AdminRoleName.WAREHOUSE_MANAGER to setOf(
                "dashboard:view",
                "warehouses:view", "warehouses:manage", "warehouses:inventory",
                "produce:view", "produce:approve",
                "logistics:view",
                "reports:view",
                "notifications:view"
            ),
            AdminRoleName.LOGISTICS_MANAGER to setOf(
                "dashboard:view",
                "logistics:view", "logistics:manage", "logistics:dispatch",
                "tracking:view", "tracking:realtime",
                "reports:view",
                "notifications:view"
            ),
            AdminRoleName.QA_OFFICER to setOf(
                "dashboard:view",
                "produce:view", "produce:approve",
                "farmers:view",
                "farms:view",
                "warehouses:view",
                "reports:view",
                "notifications:view"
            ),
            AdminRoleName.CUSTOMER_SUPPORT to setOf(
                "dashboard:view",
                "farmers:view",
                "agents:view",
                "lbc:view",
                "payments:view",
                "notifications:view", "notifications:send",
                "reports:view",
                "customers:view", "customers:edit", "customers:orders",
                "customers:wallet", "customers:notifications", "customers:reports"
            ),
            AdminRoleName.AUDITOR to setOf(
                "dashboard:view",
                "audit:view",
                "reports:view", "reports:export",
                "users:view",
                "payments:view",
                "lbc:view"
            ),
            AdminRoleName.ANALYST to setOf(
                "dashboard:view",
                "analytics:view",
                "reports:view",
                "farmers:view",
                "agents:view",
                "lbc:view"
            )
        )
    }
}
