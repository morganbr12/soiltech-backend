package com.soiltech.backend.infrastructure.seeder

import com.soiltech.backend.domain.enum.LbcStatus
import com.soiltech.backend.infrastructure.persistence.entity.LbcJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.LbcJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Order(5)
@Component
class LbcRegionSeeder(private val lbcRepository: LbcJpaRepository) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    data class LbcSeed(
        val name: String,
        val code: String,
        val region: String,
        val district: String,
        val manager: String,
        val phone: String,
        val email: String
    )

    private val seeds = listOf(
        // Ashanti — Kumasi already seeded; skip
        LbcSeed("Greater Accra Produce Buyers", "LBC-GAR-001", "Greater Accra", "Accra Metropolitan", "Kofi Mensah", "+233201000001", "lbc.greateraccra@soiltechlbc.com"),
        LbcSeed("Central Region Buyers Co.", "LBC-CEN-001", "Central", "Cape Coast Municipal", "Abena Asante", "+233201000002", "lbc.central@soiltechlbc.com"),
        LbcSeed("Western Region Buyers Ltd.", "LBC-WES-001", "Western", "Sekondi-Takoradi", "Kwame Boateng", "+233201000003", "lbc.western@soiltechlbc.com"),
        LbcSeed("Western North Buyers Co.", "LBC-WNR-001", "Western North", "Sefwi Wiawso", "Yaw Afriyie", "+233201000004", "lbc.westernnorth@soiltechlbc.com"),
        LbcSeed("Eastern Region Produce Buyers", "LBC-EAS-001", "Eastern", "Koforidua Municipal", "Akosua Frimpong", "+233201000005", "lbc.eastern@soiltechlbc.com"),
        LbcSeed("Volta Region Buyers Ltd.", "LBC-VOL-001", "Volta", "Ho Municipal", "Edem Agbeko", "+233201000006", "lbc.volta@soiltechlbc.com"),
        LbcSeed("Oti Region Buyers Co.", "LBC-OTI-001", "Oti", "Dambai", "Kafui Deku", "+233201000007", "lbc.oti@soiltechlbc.com"),
        LbcSeed("Bono Region Buyers Ltd.", "LBC-BON-001", "Bono", "Sunyani Municipal", "Kweku Owusu", "+233201000008", "lbc.bono@soiltechlbc.com"),
        LbcSeed("Bono East Buyers Co.", "LBC-BOE-001", "Bono East", "Techiman Municipal", "Ama Osei", "+233201000009", "lbc.bonoeast@soiltechlbc.com"),
        LbcSeed("Ahafo Region Buyers Ltd.", "LBC-AHA-001", "Ahafo", "Goaso Municipal", "Kwabena Acheampong", "+233201000010", "lbc.ahafo@soiltechlbc.com"),
        LbcSeed("Northern Region Buyers Co.", "LBC-NOR-001", "Northern", "Tamale Metropolitan", "Alhassan Fuseini", "+233201000011", "lbc.northern@soiltechlbc.com"),
        LbcSeed("Savannah Region Buyers Ltd.", "LBC-SAV-001", "Savannah", "Damongo", "Mahama Iddrisu", "+233201000012", "lbc.savannah@soiltechlbc.com"),
        LbcSeed("North East Buyers Co.", "LBC-NER-001", "North East", "Nalerigu", "Sulemana Yakubu", "+233201000013", "lbc.northeast@soiltechlbc.com"),
        LbcSeed("Upper East Buyers Ltd.", "LBC-UER-001", "Upper East", "Bolgatanga Municipal", "Atia Azumah", "+233201000014", "lbc.uppereast@soiltechlbc.com"),
        LbcSeed("Upper West Buyers Co.", "LBC-UWR-001", "Upper West", "Wa Municipal", "Inusah Dramani", "+233201000015", "lbc.upperwest@soiltechlbc.com"),
    )

    @Transactional
    override fun run(args: ApplicationArguments) {
        val now = LocalDateTime.now()
        var created = 0

        for (seed in seeds) {
            if (lbcRepository.existsByCode(seed.code) || lbcRepository.existsByEmail(seed.email)) {
                continue
            }
            lbcRepository.save(
                LbcJpaEntity(
                    id = UUID.randomUUID(),
                    name = seed.name,
                    code = seed.code,
                    region = seed.region,
                    district = seed.district,
                    manager = seed.manager,
                    phone = seed.phone,
                    email = seed.email,
                    agents = 0,
                    farmers = 0,
                    produceTonnes = BigDecimal.ZERO,
                    revenue = BigDecimal.ZERO,
                    compliance = 100,
                    status = LbcStatus.ACTIVE,
                    joinedDate = now
                )
            )
            created++
        }

        if (created > 0) log.info("LbcRegionSeeder created $created LBC(s)")
        else log.info("LbcRegionSeeder — all regions already seeded")
    }
}
