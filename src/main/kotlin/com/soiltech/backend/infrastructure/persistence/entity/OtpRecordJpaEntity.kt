package com.soiltech.backend.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "otp_records",
    indexes = [Index(name = "idx_otp_email", columnList = "email")]
)
class OtpRecordJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false, length = 255)
    val email: String,

    @Column(nullable = false, length = 10)
    val code: String,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,

    @Column(nullable = false)
    var used: Boolean = false,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
