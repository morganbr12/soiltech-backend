package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.repository.RefreshTokenRepository
import com.soiltech.backend.infrastructure.persistence.entity.RefreshTokenJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.RefreshTokenJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
class RefreshTokenRepositoryAdapter(
    private val jpaRepository: RefreshTokenJpaRepository
) : RefreshTokenRepository {

    @Transactional
    override fun save(userId: UUID, token: String, expiryMs: Long) {
        jpaRepository.deleteByUserId(userId)
        jpaRepository.save(
            RefreshTokenJpaEntity(
                userId = userId,
                token = token,
                expiresAt = LocalDateTime.now().plusSeconds(expiryMs / 1000)
            )
        )
    }

    override fun findByToken(token: String): Pair<UUID, Boolean>? {
        val entity = jpaRepository.findByToken(token) ?: return null
        return Pair(entity.userId, entity.revoked)
    }

    @Transactional
    override fun deleteByToken(token: String) {
        jpaRepository.findByToken(token)?.let {
            it.revoked = true
            jpaRepository.save(it)
        }
    }

    @Transactional
    override fun deleteByUserId(userId: UUID) = jpaRepository.deleteByUserId(userId)

    override fun isValid(token: String): Boolean {
        val entity = jpaRepository.findByToken(token) ?: return false
        return !entity.revoked && entity.expiresAt.isAfter(LocalDateTime.now())
    }
}
