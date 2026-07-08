package com.soiltech.backend.application.usecase.customer

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.domain.entity.ChatMessage
import com.soiltech.backend.domain.enum.ChatSenderType
import com.soiltech.backend.domain.enum.ChatStatus
import com.soiltech.backend.domain.repository.CustomerChatRepository
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class ListChatsUseCase(private val chatRepository: CustomerChatRepository) {
    fun execute(
        status: ChatStatus?,
        region: String?,
        search: String?,
        page: Int,
        limit: Int
    ): Triple<List<ChatResponse>, ChatSummaryResponse, PaginationMeta> {
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), limit.coerceIn(1, 100), Sort.by(Sort.Direction.DESC, "updatedAt"))
        val resultPage = chatRepository.findAll(status, region, search, pageable)
        val counts = chatRepository.countByStatus()
        val summary = ChatSummaryResponse(
            open = counts.getOrDefault(ChatStatus.OPEN, 0L),
            pending = counts.getOrDefault(ChatStatus.PENDING, 0L),
            resolved = counts.getOrDefault(ChatStatus.RESOLVED, 0L),
            escalated = counts.getOrDefault(ChatStatus.ESCALATED, 0L)
        )
        return Triple(resultPage.content.map { it.toResponse() }, summary, PaginationMeta.from(resultPage, page, limit))
    }
}

@Service
class GetChatMessagesUseCase(private val chatRepository: CustomerChatRepository) {
    fun execute(chatId: UUID, page: Int, limit: Int): Pair<List<ChatMessageResponse>, PaginationMeta> {
        chatRepository.findById(chatId) ?: throw NotFoundException("Chat not found with id: $chatId")
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), limit.coerceIn(1, 100))
        val resultPage = chatRepository.findMessages(chatId, pageable)
        return Pair(resultPage.content.map { it.toResponse() }, PaginationMeta.from(resultPage, page, limit))
    }
}

@Service
class SendChatMessageUseCase(private val chatRepository: CustomerChatRepository) {
    @Transactional
    fun execute(chatId: UUID, request: SendMessageRequest, agentId: UUID, agentName: String): ChatMessageResponse {
        val chat = chatRepository.findById(chatId) ?: throw NotFoundException("Chat not found with id: $chatId")
        if (chat.status == ChatStatus.RESOLVED)
            throw BadRequestException("Cannot send messages to a resolved chat")
        val now = LocalDateTime.now()
        val message = chatRepository.saveMessage(
            ChatMessage(
                id = UUID.randomUUID(),
                chatId = chatId,
                senderType = ChatSenderType.AGENT,
                senderId = agentId,
                senderName = agentName,
                message = request.message,
                sentAt = now
            )
        )
        val updatedChat = chat.copy(lastMessage = request.message, lastMessageAt = now, updatedAt = now)
        chatRepository.save(updatedChat)
        return message.toResponse()
    }
}

@Service
class ResolveChatUseCase(private val chatRepository: CustomerChatRepository) {
    @Transactional
    fun execute(chatId: UUID): ChatResponse {
        val chat = chatRepository.findById(chatId) ?: throw NotFoundException("Chat not found with id: $chatId")
        if (chat.status == ChatStatus.RESOLVED) throw BadRequestException("Chat is already resolved")
        return chatRepository.save(chat.copy(status = ChatStatus.RESOLVED, updatedAt = LocalDateTime.now())).toResponse()
    }
}

@Service
class EscalateChatUseCase(private val chatRepository: CustomerChatRepository) {
    @Transactional
    fun execute(chatId: UUID, request: EscalateChatRequest): ChatResponse {
        val chat = chatRepository.findById(chatId) ?: throw NotFoundException("Chat not found with id: $chatId")
        if (chat.status == ChatStatus.ESCALATED) throw BadRequestException("Chat is already escalated")
        return chatRepository.save(chat.copy(
            status = ChatStatus.ESCALATED,
            escalationReason = request.reason,
            updatedAt = LocalDateTime.now()
        )).toResponse()
    }
}

@Service
class AssignChatUseCase(private val chatRepository: CustomerChatRepository) {
    @Transactional
    fun execute(chatId: UUID, request: AssignChatRequest): ChatResponse {
        val chat = chatRepository.findById(chatId) ?: throw NotFoundException("Chat not found with id: $chatId")
        return chatRepository.save(chat.copy(
            agentId = request.agentId,
            status = ChatStatus.OPEN,
            updatedAt = LocalDateTime.now()
        )).toResponse()
    }
}

// ── Mappers ────────────────────────────────────────────────────────────────────

private fun com.soiltech.backend.domain.entity.CustomerChat.toResponse() = ChatResponse(
    id = id, customerId = customerId, customerName = customerName, agentId = agentId, agentName = agentName,
    lastMessage = lastMessage, lastMessageAt = lastMessageAt, unreadCount = unreadCount,
    status = status, topic = topic, region = region
)

private fun ChatMessage.toResponse() = ChatMessageResponse(
    id = id, chatId = chatId, senderType = senderType, senderId = senderId,
    senderName = senderName, message = message, sentAt = sentAt
)
