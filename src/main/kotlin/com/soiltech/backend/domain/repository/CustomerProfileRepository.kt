package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.CustomerProfile
import java.util.UUID

interface CustomerProfileRepository {
    fun findById(id: UUID): CustomerProfile?
    fun findByUserId(userId: UUID): CustomerProfile?
    fun save(profile: CustomerProfile): CustomerProfile
    fun update(profile: CustomerProfile): CustomerProfile
}
