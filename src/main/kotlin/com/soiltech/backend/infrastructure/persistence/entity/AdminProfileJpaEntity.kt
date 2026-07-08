package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.AdminProfile
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "admin_profiles",
    indexes = [Index(name = "idx_admin_profiles_user_id", columnList = "userId", unique = true)]
)
class AdminProfileJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val userId: UUID,

    @Column(nullable = false, length = 255)
    var fullName: String,

    @Column(nullable = false, length = 255)
    val email: String,

    @Column(length = 20)
    var phone: String? = null,

    @Column(length = 100)
    var region: String? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_role_id", nullable = false)
    var adminRole: AdminRoleJpaEntity,

    @Column(nullable = false)
    var isActive: Boolean = true
) : BaseJpaEntity() {

    fun toDomain(): AdminProfile = AdminProfile(
        id = id!!,
        userId = userId,
        fullName = fullName,
        email = email,
        phone = phone ?: "",
        region = region,
        adminRoleId = adminRole.id!!,
        adminRoleName = adminRole.name,
        permissions = adminRole.permissions.toSet(),
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
