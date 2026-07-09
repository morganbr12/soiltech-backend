package com.soiltech.backend.application.usecase.customer

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.domain.entity.ChatMessage
import com.soiltech.backend.domain.entity.CustomerChat
import com.soiltech.backend.domain.enum.ChatSenderType
import com.soiltech.backend.domain.enum.ChatStatus
import com.soiltech.backend.domain.repository.CustomerChatRepository
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.domain.repository.ProduceListingRepository
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.ForbiddenException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class StartLbcChatUseCase(
    private val chatRepository: CustomerChatRepository,
    private val customerProfileRepository: CustomerProfileRepository,
    private val produceListingRepository: ProduceListingRepository
) {
    @Transactional
    fun execute(request: StartChatRequest, userId: UUID): ChatResponse {
        val customer = customerProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Customer profile not found")

        val listing = produceListingRepository.findById(request.produceListingId)
            ?: throw NotFoundException("Produce listing not found")

        val lbcId = listing.lbcId
            ?: throw BadRequestException("This produce listing is not associated with an LBC")
        val lbcName = listing.lbcName ?: "LBC"

        val existing = chatRepository.findByCustomerIdAndLbcId(customer.id, lbcId)
        if (existing != null) {
            if (request.message != null) {
                sendMessage(existing.id, request.message, customer.id, customer.fullName)
            }
            return existing.toResponse()
        }

        val now = LocalDateTime.now()
        val chat = chatRepository.save(
            CustomerChat(
                id = UUID.randomUUID(),
                customerId = customer.id,
                customerName = customer.fullName,
                agentId = null,
                agentName = null,
                lbcId = lbcId,
                lbcName = lbcName,
                lastMessage = request.message,
                lastMessageAt = if (request.message != null) now else null,
                unreadCount = if (request.message != null) 1 else 0,
                status = ChatStatus.OPEN,
                topic = "Enquiry about ${listing.cropType}",
                region = listing.region,
                escalationReason = null,
                createdAt = now,
                updatedAt = now
            )
        )

        if (request.message != null) {
            sendMessage(chat.id, request.message, customer.id, customer.fullName)
        }

        return chat.toResponse()
    }

    private fun sendMessage(chatId: UUID, message: String, customerId: UUID, customerName: String) {
        chatRepository.saveMessage(
            ChatMessage(
                id = UUID.randomUUID(),
                chatId = chatId,
                senderType = ChatSenderType.CUSTOMER,
                senderId = customerId,
                senderName = customerName,
                message = message,
                sentAt = LocalDateTime.now()
            )
        )
    }
}

@Service
class CustomerListChatsUseCase(private val chatRepository: CustomerChatRepository) {
    fun execute(userId: UUID, customerProfileRepository: CustomerProfileRepository): List<ChatResponse> {
        val customer = customerProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Customer profile not found")
        return chatRepository.findByCustomerId(customer.id).map { it.toResponse() }
    }
}

@Service
class CustomerGetMessagesUseCase(private val chatRepository: CustomerChatRepository) {
    fun execute(chatId: UUID, userId: UUID, customerProfileRepository: CustomerProfileRepository, page: Int, limit: Int): Pair<List<ChatMessageResponse>, PaginationMeta> {
        val chat = chatRepository.findById(chatId)
            ?: throw NotFoundException("Chat not found")
        val customer = customerProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Customer profile not found")
        if (chat.customerId != customer.id)
            throw ForbiddenException("You do not have access to this chat")

        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), limit.coerceIn(1, 100))
        val result = chatRepository.findMessages(chatId, pageable)
        return result.content.map {
            ChatMessageResponse(
                id = it.id, chatId = it.chatId, senderType = it.senderType,
                senderId = it.senderId, senderName = it.senderName,
                message = it.message, sentAt = it.sentAt
            )
        } to PaginationMeta.from(result, page, limit)
    }
}

@Service
class CustomerSendMessageUseCase(private val chatRepository: CustomerChatRepository) {
    @Transactional
    fun execute(chatId: UUID, request: SendMessageRequest, userId: UUID, customerProfileRepository: CustomerProfileRepository): ChatMessageResponse {
        val chat = chatRepository.findById(chatId)
            ?: throw NotFoundException("Chat not found")
        val customer = customerProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Customer profile not found")
        if (chat.customerId != customer.id)
            throw ForbiddenException("You do not have access to this chat")
        if (chat.status == ChatStatus.RESOLVED)
            throw BadRequestException("This chat has been resolved. Start a new enquiry to continue.")

        val now = LocalDateTime.now()
        val message = chatRepository.saveMessage(
            ChatMessage(
                id = UUID.randomUUID(),
                chatId = chatId,
                senderType = ChatSenderType.CUSTOMER,
                senderId = customer.id,
                senderName = customer.fullName,
                message = request.message,
                sentAt = now
            )
        )
        chatRepository.save(chat.copy(
            lastMessage = request.message,
            lastMessageAt = now,
            unreadCount = chat.unreadCount + 1,
            updatedAt = now
        ))
        return ChatMessageResponse(
            id = message.id, chatId = message.chatId, senderType = message.senderType,
            senderId = message.senderId, senderName = message.senderName,
            message = message.message, sentAt = message.sentAt
        )
    }
}
