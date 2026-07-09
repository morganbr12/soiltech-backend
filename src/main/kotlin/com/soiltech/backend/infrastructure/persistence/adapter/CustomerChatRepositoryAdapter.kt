package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.ChatMessage
import com.soiltech.backend.domain.entity.CustomerChat
import com.soiltech.backend.domain.enum.ChatStatus
import com.soiltech.backend.domain.repository.CustomerChatRepository
import com.soiltech.backend.infrastructure.persistence.entity.ChatMessageJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.CustomerChatJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.ChatMessageJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.CustomerChatJpaRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CustomerChatRepositoryAdapter(
    private val chatJpa: CustomerChatJpaRepository,
    private val messageJpa: ChatMessageJpaRepository
) : CustomerChatRepository {

    override fun findById(id: UUID): CustomerChat? =
        chatJpa.findById(id).orElse(null)?.toDomain()

    override fun findAll(status: ChatStatus?, region: String?, search: String?, pageable: Pageable): Page<CustomerChat> {
        val spec = Specification<CustomerChatJpaEntity> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()
            status?.let { predicates.add(cb.equal(root.get<ChatStatus>("status"), it)) }
            region?.let { predicates.add(cb.equal(cb.lower(root.get("region")), it.lowercase())) }
            search?.let { q ->
                val like = "%${q.lowercase()}%"
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("customerName")), like),
                    cb.like(cb.lower(root.get("topic")), like),
                    cb.like(cb.lower(root.get("lastMessage")), like)
                ))
            }
            cb.and(*predicates.toTypedArray())
        }
        return chatJpa.findAll(spec, pageable).map { it.toDomain() }
    }

    override fun save(chat: CustomerChat): CustomerChat {
        val existing = chatJpa.findById(chat.id).orElse(null)
        return if (existing != null) {
            existing.apply {
                agentId = chat.agentId
                agentName = chat.agentName
                lbcId = chat.lbcId
                lbcName = chat.lbcName
                lastMessage = chat.lastMessage
                lastMessageAt = chat.lastMessageAt
                unreadCount = chat.unreadCount
                status = chat.status
                topic = chat.topic
                escalationReason = chat.escalationReason
            }
            chatJpa.save(existing).toDomain()
        } else {
            chatJpa.save(CustomerChatJpaEntity.fromDomain(chat)).toDomain()
        }
    }

    override fun countByStatus(): Map<ChatStatus, Long> {
        return chatJpa.countGroupByStatus().associate { row ->
            val status = when (val v = row[0]) {
                is ChatStatus -> v
                is String -> ChatStatus.entries.firstOrNull { it.name == v } ?: return@associate null to 0L
                else -> return@associate null to 0L
            }
            status to (row[1] as Number).toLong()
        }.filterKeys { it != null } as Map<ChatStatus, Long>
    }

    override fun findMessages(chatId: UUID, pageable: Pageable): Page<ChatMessage> =
        messageJpa.findByChatIdOrderBySentAtAsc(chatId, pageable).map { it.toDomain() }

    override fun saveMessage(message: ChatMessage): ChatMessage =
        messageJpa.save(ChatMessageJpaEntity.fromDomain(message)).toDomain()

    override fun findByCustomerId(customerId: UUID): List<CustomerChat> =
        chatJpa.findByCustomerIdOrderByUpdatedAtDesc(customerId).map { it.toDomain() }

    override fun findByCustomerIdAndLbcId(customerId: UUID, lbcId: UUID): CustomerChat? =
        chatJpa.findByCustomerIdAndLbcId(customerId, lbcId)?.toDomain()
}
