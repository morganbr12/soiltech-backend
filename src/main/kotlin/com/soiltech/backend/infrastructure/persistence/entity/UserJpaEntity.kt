package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.User
import com.soiltech.backend.domain.enum.UserRole
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_users_email", columnList = "email", unique = true),
        Index(name = "idx_users_phone", columnList = "phone", unique = true)
    ]
)
class UserJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(unique = true, nullable = false, length = 255)
    val email: String,

    @Column(unique = true, nullable = false, length = 20)
    var phone: String,

    @Column(nullable = false)
    var passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: UserRole,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column
    var lastLoginAt: LocalDateTime? = null
) : BaseJpaEntity() {


    fun toDomain(): User = User(
        id = id!!,
        email = email,
        phone = phone,
        passwordHash = passwordHash,
        role = role,
        isActive = isActive,
        lastLoginAt = lastLoginAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(user: User): UserJpaEntity = UserJpaEntity(
            id = user.id,
            email = user.email,
            phone = user.phone,
            passwordHash = user.passwordHash,
            role = user.role,
            isActive = user.isActive,
            lastLoginAt = user.lastLoginAt
        )
    }
}
