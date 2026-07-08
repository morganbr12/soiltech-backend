package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.ChatMessage
import com.soiltech.backend.domain.entity.CustomerChat
import com.soiltech.backend.domain.enum.ChatStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CustomerChatRepository {
    fun findById(id: UUID): CustomerChat?
    fun findAll(status: ChatStatus?, region: String?, search: String?, pageable: Pageable): Page<CustomerChat>
    fun save(chat: CustomerChat): CustomerChat
    fun countByStatus(): Map<ChatStatus, Long>

    fun findMessages(chatId: UUID, pageable: Pageable): Page<ChatMessage>
    fun saveMessage(message: ChatMessage): ChatMessage
}
