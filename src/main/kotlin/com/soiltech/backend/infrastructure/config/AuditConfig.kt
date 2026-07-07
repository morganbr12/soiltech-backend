package com.soiltech.backend.infrastructure.config

import com.soiltech.backend.infrastructure.security.UserPrincipal
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional
import java.util.UUID

@Configuration
class AuditConfig {

    @Bean("auditorAware")
    fun auditorAware(): AuditorAware<UUID> = AuditorAware {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            return@AuditorAware Optional.empty()
        }
        val principal = authentication.principal as? UserPrincipal
            ?: return@AuditorAware Optional.empty()
        Optional.of(principal.id)
    }
}
