package com.soiltech.backend.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID
import java.util.UUID.randomUUID

@Entity
@Table(
    name = "refresh_tokens",
    indexes = [
        Index(name = "idx_refresh_token", columnList = "token", unique = true),
        Index(name = "idx_refresh_user_id", columnList = "userId")
    ]
)
class RefreshTokenJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = randomUUID(),

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false, unique = true, length = 1000)
    val token: String,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,

    @Column(nullable = false)
    var revoked: Boolean = false,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
