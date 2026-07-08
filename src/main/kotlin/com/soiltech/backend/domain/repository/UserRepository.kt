package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.User
import java.util.UUID

interface UserRepository {
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun findByPhone(phone: String): User?
    fun save(user: User): User
    fun existsByEmail(email: String): Boolean
    fun existsByPhone(phone: String): Boolean
    fun delete(id: UUID)
}
