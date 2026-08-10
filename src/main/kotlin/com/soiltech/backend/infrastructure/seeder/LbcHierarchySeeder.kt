package com.soiltech.backend.infrastructure.seeder

import com.soiltech.backend.domain.enum.AgentStatus
import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.domain.enum.FarmerStatus
import com.soiltech.backend.domain.enum.SyncStatus
import com.soiltech.backend.infrastructure.persistence.entity.AgentJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.FarmJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.FarmerJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.ProduceRecordJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.AgentJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.FarmJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.FarmerJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.LbcJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.ProduceRecordJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Order(6)
@Component
class LbcHierarchySeeder(
    private val lbcRepository: LbcJpaRepository,
    private val agentRepository: AgentJpaRepository,
    private val farmerRepository: FarmerJpaRepository,
    private val farmRepository: FarmJpaRepository,
    private val produceRecordRepository: ProduceRecordJpaRepository
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    private val firstNames = listOf(
        "Kwame", "Kofi", "Ama", "Akosua", "Yaw", "Abena", "Kweku", "Adwoa",
        "Kojo", "Akua", "Fiifi", "Efua", "Nana", "Esi", "Kwabena"
    )
    private val lastNames = listOf(
        "Mensah", "Asante", "Boateng", "Owusu", "Acheampong", "Frimpong",
        "Amoah", "Appiah", "Osei", "Darko", "Kyei", "Antwi", "Bonsu", "Ofori", "Sarpong"
    )
    private val crops = listOf(
        Triple("Cocoa", "Forastero", "A"),
        Triple("Cashew", "Jumbo", "B"),
        Triple("Coffee", "Robusta", "A"),
        Triple("Maize", "Yellow Dent", "A"),
        Triple("Yam", "Puna", "B"),
        Triple("Cassava", "Bankye Hemaa", "A"),
        Triple("Groundnut", "Chinese", "B"),
        Triple("Soybean", "Jenguma", "A"),
        Triple("Palm", "Tenera", "A"),
        Triple("Shea", "Wild", "B")
    )
    private val communities = listOf(
        "Akyem", "Nkwanta", "Afrancho", "Buoho", "Domeabra",
        "Amoawi", "Nsuta", "Twedie", "Bechem", "Kwabre"
    )

    @Transactional
    override fun run(args: ApplicationArguments) {
        val lbcs = lbcRepository.findAll()
        if (lbcs.isEmpty()) {
            log.warn("LbcHierarchySeeder — no LBCs found, skipping")
            return
        }

        val now = LocalDateTime.now()
        var agentCount = 0
        var farmerCount = 0
        var farmCount = 0
        var produceCount = 0

        // Use a global counter to guarantee unique phones/emails/codes across all seeds
        var globalAgentIdx = agentRepository.count().toInt()
        var globalFarmerIdx = farmerRepository.count().toInt()

        for (lbc in lbcs) {
            // Skip if this LBC already has agents
            if (agentRepository.existsByAgentCode("AGT-${lbc.code}-01")) continue

            val regionCode = lbc.code.substringAfter("LBC-").substringBefore("-")

            for (agentSlot in 1..3) {
                globalAgentIdx++
                val firstName = firstNames[(globalAgentIdx - 1) % firstNames.size]
                val lastName = lastNames[agentSlot % lastNames.size]
                val agentCode = "AGT-$regionCode-%02d".format(agentSlot)
                val agentPhone = "+2335%08d".format(globalAgentIdx)
                val agentEmail = "agent.${regionCode.lowercase()}.$agentSlot@soiltechlbc.com"

                if (agentRepository.existsByPhone(agentPhone) || agentRepository.existsByEmail(agentEmail)) continue

                val agent = agentRepository.save(
                    AgentJpaEntity(
                        id = UUID.randomUUID(),
                        firstName = firstName,
                        lastName = lastName,
                        phone = agentPhone,
                        email = agentEmail,
                        agentCode = agentCode,
                        lbc = lbc,
                        region = lbc.region,
                        district = lbc.district,
                        status = AgentStatus.ACTIVE,
                        latitude = null,
                        longitude = null,
                        lastSeen = null,
                        joinedDate = now
                    )
                )
                agentCount++

                for (farmerSlot in 1..3) {
                    globalFarmerIdx++
                    val fFirstName = firstNames[(globalFarmerIdx + 3) % firstNames.size]
                    val fLastName = lastNames[(globalFarmerIdx) % lastNames.size]
                    val farmerCode = "FMR-%06d".format(globalFarmerIdx)
                    val farmerPhone = "+2332%08d".format(globalFarmerIdx)
                    val community = communities[(globalFarmerIdx - 1) % communities.size]
                    val cropTuple = crops[(globalFarmerIdx - 1) % crops.size]

                    if (farmerRepository.existsByPhone(farmerPhone) || farmerRepository.existsByFarmerCode(farmerCode)) continue

                    val farmer = farmerRepository.save(
                        FarmerJpaEntity(
                            id = UUID.randomUUID(),
                            farmerCode = farmerCode,
                            firstName = fFirstName,
                            lastName = fLastName,
                            phone = farmerPhone,
                            email = "farmer${globalFarmerIdx}@soiltechlbc.com",
                            nationalId = "GHA-%08d".format(globalFarmerIdx),
                            agentId = agent.id!!,
                            lbcId = lbc.id!!,
                            region = lbc.region,
                            district = lbc.district,
                            community = community,
                            status = FarmerStatus.APPROVED,
                            kycVerified = true,
                            cropTypesRaw = cropTuple.first,
                            fullName = "$fFirstName $fLastName",
                            syncStatus = "SYNCED",
                            joinedDate = now
                        )
                    )
                    farmerCount++

                    // Farm
                    val farm = farmRepository.save(
                        FarmJpaEntity(
                            id = UUID.randomUUID(),
                            farmerId = farmer.id!!,
                            name = "${cropTuple.first} Farm – ${farmer.firstName}",
                            sizeHectares = (1.5 + (globalFarmerIdx % 5) * 0.5),
                            cropType = cropTuple.first,
                            location = "$community, ${lbc.district}",
                            latitude = null,
                            longitude = null,
                            estimatedYieldKg = ((globalFarmerIdx % 5 + 1) * 500).toDouble(),
                            lastHarvestDate = LocalDate.now().minusMonths(((globalFarmerIdx % 4 + 1).toLong())),
                            photosRaw = null
                        )
                    )
                    farmCount++

                    // 2 produce records per farmer
                    for (produceSlot in 1..2) {
                        val qty = BigDecimal.valueOf(((globalFarmerIdx % 10 + produceSlot) * 50).toLong())
                        val price = BigDecimal.valueOf(if (cropTuple.first == "Cocoa") 25 else 12)
                        produceRecordRepository.save(
                            ProduceRecordJpaEntity(
                                id = UUID.randomUUID(),
                                farmerId = farmer.id!!,
                                farmId = farm.id,
                                agentId = agent.id!!,
                                cropType = cropTuple.first,
                                cropVariety = cropTuple.second,
                                grade = cropTuple.third,
                                quantityKg = qty,
                                pricePerKg = price,
                                totalAmount = qty.multiply(price),
                                status = CollectionStatus.COLLECTED,
                                collectedAt = now.minusDays((produceSlot * 7).toLong()),
                                notes = "Seeded record – ${lbc.region}",
                                photosRaw = null,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                        produceCount++
                    }
                }
            }
        }

        log.info(
            "LbcHierarchySeeder done — agents: {}, farmers: {}, farms: {}, produce records: {}",
            agentCount, farmerCount, farmCount, produceCount
        )
    }
}
