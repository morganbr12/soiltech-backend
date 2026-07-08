package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.AdminRole
import com.soiltech.backend.domain.enum.AdminRoleName
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "admin_roles")
class AdminRoleJpaEntity(
    @Id
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    val name: AdminRoleName,

    @Column(nullable = false, length = 100)
    val label: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "admin_role_permissions",
        joinColumns = [JoinColumn(name = "role_id")]
    )
    @Column(name = "permission", length = 100)
    val permissions: MutableSet<String> = mutableSetOf()
) : BaseJpaEntity() {

    fun toDomain(): AdminRole = AdminRole(
        id = id!!,
        name = name,
        label = label,
        permissions = permissions.toSet(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
