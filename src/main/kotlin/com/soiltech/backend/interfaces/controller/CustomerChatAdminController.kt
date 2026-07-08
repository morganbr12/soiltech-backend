package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.application.usecase.customer.*
import com.soiltech.backend.domain.enum.ChatStatus
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/customers/chats")
class CustomerChatAdminController(
    private val listChatsUseCase: ListChatsUseCase,
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val resolveChatUseCase: ResolveChatUseCase,
    private val escalateChatUseCase: EscalateChatUseCase,
    private val assignChatUseCase: AssignChatUseCase
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:chats')")
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) search: String?
    ): ResponseEntity<ApiResponse<List<ChatResponse>>> {
        val chatStatus = status?.let { ChatStatus.fromValue(it) }
        val (items, summary, meta) = listChatsUseCase.execute(chatStatus, region, search, page, limit)
        return ResponseEntity.ok(ApiResponse.success(items, meta = meta, summary = summary))
    }

    @GetMapping("/{chatId}/messages")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:chats')")
    fun messages(
        @PathVariable chatId: UUID,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "50") limit: Int
    ): ResponseEntity<ApiResponse<List<ChatMessageResponse>>> {
        val (items, meta) = getChatMessagesUseCase.execute(chatId, page, limit)
        return ResponseEntity.ok(ApiResponse.success(items, meta = meta))
    }

    @PostMapping("/{chatId}/messages")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:chats')")
    fun sendMessage(
        @PathVariable chatId: UUID,
        @Valid @RequestBody request: SendMessageRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<ChatMessageResponse>> {
        val data = sendChatMessageUseCase.execute(chatId, request, principal.id, principal.username)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @PatchMapping("/{chatId}/resolve")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:chats')")
    fun resolve(@PathVariable chatId: UUID): ResponseEntity<ApiResponse<ChatResponse>> {
        val data = resolveChatUseCase.execute(chatId)
        return ResponseEntity.ok(ApiResponse.success(data, "Chat resolved"))
    }

    @PatchMapping("/{chatId}/escalate")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:chats')")
    fun escalate(
        @PathVariable chatId: UUID,
        @RequestBody(required = false) request: EscalateChatRequest?
    ): ResponseEntity<ApiResponse<ChatResponse>> {
        val data = escalateChatUseCase.execute(chatId, request ?: EscalateChatRequest())
        return ResponseEntity.ok(ApiResponse.success(data, "Chat escalated"))
    }

    @PatchMapping("/{chatId}/assign")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:chats')")
    fun assign(
        @PathVariable chatId: UUID,
        @Valid @RequestBody request: AssignChatRequest
    ): ResponseEntity<ApiResponse<ChatResponse>> {
        val data = assignChatUseCase.execute(chatId, request)
        return ResponseEntity.ok(ApiResponse.success(data, "Chat assigned"))
    }
}
