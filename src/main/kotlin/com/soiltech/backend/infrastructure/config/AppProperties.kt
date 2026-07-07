package com.soiltech.backend.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "application.otp")
data class OtpProperties(
    val expiryMinutes: Long = 10
)

@Configuration
@EnableConfigurationProperties(OtpProperties::class)
class AppProperties
