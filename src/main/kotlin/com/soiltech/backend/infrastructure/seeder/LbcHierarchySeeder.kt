package com.soiltech.backend.infrastructure.seeder

import com.soiltech.backend.infrastructure.service.SeedService
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(6)
@Component
class LbcHierarchySeeder(private val seedService: SeedService) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        val result = seedService.seedHierarchy()
        if (result.agents == 0 && result.farmers == 0) {
            log.info("LbcHierarchySeeder — all LBCs already seeded, nothing to do")
        }
    }
}
