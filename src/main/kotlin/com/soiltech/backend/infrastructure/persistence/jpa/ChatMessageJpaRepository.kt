package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.ChatMessageJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ChatMessageJpaRepository : JpaRepository<ChatMessageJpaEntity, UUID> {
    fun findByChatIdOrderBySentAtAsc(chatId: UUID, pageable: Pageable): Page<ChatMessageJpaEntity>
}
