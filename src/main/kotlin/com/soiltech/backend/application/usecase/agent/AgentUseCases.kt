package com.soiltech.backend.application.usecase.agent

import com.soiltech.backend.application.dto.agent.AgentProfileDto
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.interfaces.exception.NotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetAgentProfileUseCase(
    private val agentProfileRepository: AgentProfileRepository
) {
    fun execute(userId: UUID): AgentProfileDto {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        return AgentProfileDto(
            id = profile.id,
            userId = profile.userId,
            fullName = profile.fullName,
            firstName = profile.fullName.substringBefore(" ").ifBlank { profile.fullName },
            lastName = profile.fullName.substringAfter(" ", ""),
            agentCode = profile.agentCode,
            region = profile.region,
            createdAt = profile.createdAt,
            updatedAt = profile.updatedAt
        )
    }
}
