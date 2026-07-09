package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.application.usecase.customer.*
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/chats")
@PreAuthorize("hasRole('CUSTOMER')")
class CustomerChatController(
    private val startLbcChatUseCase: StartLbcChatUseCase,
    private val customerListChatsUseCase: CustomerListChatsUseCase,
    private val customerGetMessagesUseCase: CustomerGetMessagesUseCase,
    private val customerSendMessageUseCase: CustomerSendMessageUseCase,
    private val customerProfileRepository: CustomerProfileRepository
) {

    @PostMapping
    fun startChat(
        @Valid @RequestBody request: StartChatRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<ChatResponse>> {
        val data = startLbcChatUseCase.execute(request, principal.id)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Chat started"))
    }

    @GetMapping
    fun listChats(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<ChatResponse>>> {
        val data = customerListChatsUseCase.execute(principal.id, customerProfileRepository)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @GetMapping("/{chatId}/messages")
    fun getMessages(
        @PathVariable chatId: UUID,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "50") limit: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<ChatMessageResponse>>> {
        val (data, meta) = customerGetMessagesUseCase.execute(chatId, principal.id, customerProfileRepository, page, limit)
        return ResponseEntity.ok(ApiResponse.success(data, meta = meta))
    }

    @PostMapping("/{chatId}/messages")
    fun sendMessage(
        @PathVariable chatId: UUID,
        @Valid @RequestBody request: SendMessageRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<ChatMessageResponse>> {
        val data = customerSendMessageUseCase.execute(chatId, request, principal.id, customerProfileRepository)
        return ResponseEntity.ok(ApiResponse.success(data))
    }
}
