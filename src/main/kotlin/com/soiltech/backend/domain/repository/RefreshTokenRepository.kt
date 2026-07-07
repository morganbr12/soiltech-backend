package com.soiltech.backend.domain.repository

import java.util.UUID

interface RefreshTokenRepository {
    fun save(userId: UUID, token: String, expiryMs: Long)
    fun findByToken(token: String): Pair<UUID, Boolean>?
    fun deleteByToken(token: String)
    fun deleteByUserId(userId: UUID)
    fun isValid(token: String): Boolean
}
