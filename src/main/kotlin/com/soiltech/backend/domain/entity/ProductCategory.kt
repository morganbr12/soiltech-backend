package com.soiltech.backend.domain.entity

import java.time.LocalDateTime
import java.util.UUID

data class ProductCategory(
    val id: UUID,
    val name: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
