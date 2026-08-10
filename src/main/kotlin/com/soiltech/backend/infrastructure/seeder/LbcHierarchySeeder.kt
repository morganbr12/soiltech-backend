package com.soiltech.backend.infrastructure.seeder

import com.soiltech.backend.domain.enum.AgentStatus
import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.domain.enum.FarmerStatus
import com.soiltech.backend.domain.enum.ProduceListingStatus
import com.soiltech.backend.domain.enum.SyncStatus
import com.soiltech.backend.infrastructure.persistence.entity.AgentJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.FarmJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.FarmerJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.ProduceListingJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.ProduceRecordJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.ProductCategoryJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.ProductJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.AgentJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.FarmJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.FarmerJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.LbcJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.ProduceListingJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.ProduceRecordJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.ProductCategoryJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.ProductJpaRepository
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
    private val produceRecordRepository: ProduceRecordJpaRepository,
    private val produceListingRepository: ProduceListingJpaRepository,
    private val productCategoryRepository: ProductCategoryJpaRepository,
    private val productRepository: ProductJpaRepository
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

    // crop -> (variety, grade, pricePerKg)
    private val crops = listOf(
        CropSpec("Cocoa",     "Forastero",    "A", BigDecimal("25.00")),
        CropSpec("Cashew",    "Jumbo",         "B", BigDecimal("18.00")),
        CropSpec("Coffee",    "Robusta",       "A", BigDecimal("20.00")),
        CropSpec("Maize",     "Yellow Dent",   "A", BigDecimal("8.00")),
        CropSpec("Yam",       "Puna",          "B", BigDecimal("12.00")),
        CropSpec("Cassava",   "Bankye Hemaa",  "A", BigDecimal("6.00")),
        CropSpec("Groundnut", "Chinese",       "B", BigDecimal("15.00")),
        CropSpec("Soybean",   "Jenguma",       "A", BigDecimal("10.00")),
        CropSpec("Palm",      "Tenera",        "A", BigDecimal("9.00")),
        CropSpec("Shea",      "Wild",          "B", BigDecimal("14.00"))
    )

    private val communities = listOf(
        "Akyem", "Nkwanta", "Afrancho", "Buoho", "Domeabra",
        "Amoawi", "Nsuta", "Twedie", "Bechem", "Kwabre"
    )

    data class CropSpec(val name: String, val variety: String, val grade: String, val price: BigDecimal)

    // Cache categories so we upsert once per crop name, not once per produce record
    private val categoryCache = mutableMapOf<String, UUID>()

    @Transactional
    override fun run(args: ApplicationArguments) {
        val lbcs = lbcRepository.findAll()
        if (lbcs.isEmpty()) {
            log.warn("LbcHierarchySeeder — no LBCs found, skipping")
            return
        }

        val now = LocalDateTime.now()
        var agentCount = 0; var farmerCount = 0; var farmCount = 0
        var produceCount = 0; var listingCount = 0; var productCount = 0

        var globalAgentIdx  = agentRepository.count().toInt()
        var globalFarmerIdx = farmerRepository.count().toInt()

        for (lbc in lbcs) {
            if (agentRepository.existsByAgentCode("AGT-${lbc.code}-01")) continue

            val regionCode = lbc.code.substringAfter("LBC-").substringBefore("-")

            for (agentSlot in 1..3) {
                globalAgentIdx++
                val agentCode  = "AGT-$regionCode-%02d".format(agentSlot)
                val agentPhone = "+2335%08d".format(globalAgentIdx)
                val agentEmail = "agent.${regionCode.lowercase()}.$agentSlot@soiltechlbc.com"

                if (agentRepository.existsByPhone(agentPhone) || agentRepository.existsByEmail(agentEmail)) continue

                val agent = agentRepository.save(
                    AgentJpaEntity(
                        id = UUID.randomUUID(),
                        firstName = firstNames[(globalAgentIdx - 1) % firstNames.size],
                        lastName  = lastNames[agentSlot % lastNames.size],
                        phone = agentPhone,
                        email = agentEmail,
                        agentCode = agentCode,
                        lbc = lbc,
                        region = lbc.region,
                        district = lbc.district,
                        status = AgentStatus.ACTIVE,
                        latitude = null, longitude = null, lastSeen = null,
                        joinedDate = now
                    )
                )
                agentCount++

                for (farmerSlot in 1..3) {
                    globalFarmerIdx++
                    val fFirst      = firstNames[(globalFarmerIdx + 3) % firstNames.size]
                    val fLast       = lastNames[globalFarmerIdx % lastNames.size]
                    val farmerCode  = "FMR-%06d".format(globalFarmerIdx)
                    val farmerPhone = "+2332%08d".format(globalFarmerIdx)
                    val community   = communities[(globalFarmerIdx - 1) % communities.size]
                    val crop        = crops[(globalFarmerIdx - 1) % crops.size]

                    if (farmerRepository.existsByPhone(farmerPhone) || farmerRepository.existsByFarmerCode(farmerCode)) continue

                    val farmer = farmerRepository.save(
                        FarmerJpaEntity(
                            id = UUID.randomUUID(),
                            farmerCode = farmerCode,
                            firstName = fFirst,
                            lastName  = fLast,
                            phone = farmerPhone,
                            email = "farmer${globalFarmerIdx}@soiltechlbc.com",
                            nationalId = "GHA-%08d".format(globalFarmerIdx),
                            agentId = agent.id!!,
                            lbcId   = lbc.id!!,
                            region   = lbc.region,
                            district = lbc.district,
                            community = community,
                            status = FarmerStatus.APPROVED,
                            kycVerified = true,
                            cropTypesRaw = crop.name,
                            fullName = "$fFirst $fLast",
                            syncStatus = "SYNCED",
                            joinedDate = now
                        )
                    )
                    farmerCount++

                    val farm = farmRepository.save(
                        FarmJpaEntity(
                            id = UUID.randomUUID(),
                            farmerId = farmer.id!!,
                            name = "${crop.name} Farm – ${farmer.firstName}",
                            sizeHectares = 1.5 + (globalFarmerIdx % 5) * 0.5,
                            cropType = crop.name,
                            location = "$community, ${lbc.district}",
                            latitude = null, longitude = null,
                            estimatedYieldKg = ((globalFarmerIdx % 5 + 1) * 500).toDouble(),
                            lastHarvestDate = LocalDate.now().minusMonths((globalFarmerIdx % 4 + 1).toLong()),
                            photosRaw = null
                        )
                    )
                    farmCount++

                    for (slot in 1..2) {
                        val qty   = BigDecimal.valueOf(((globalFarmerIdx % 10 + slot) * 50).toLong())
                        val total = qty.multiply(crop.price)

                        val record = produceRecordRepository.save(
                            ProduceRecordJpaEntity(
                                id = UUID.randomUUID(),
                                farmerId = farmer.id!!,
                                farmId   = farm.id,
                                agentId  = agent.id!!,
                                cropType    = crop.name,
                                cropVariety = crop.variety,
                                grade       = crop.grade,
                                quantityKg  = qty,
                                pricePerKg  = crop.price,
                                totalAmount = total,
                                status      = CollectionStatus.COLLECTED,
                                collectedAt = now.minusDays((slot * 7).toLong()),
                                notes       = "Seeded – ${lbc.region}",
                                photosRaw   = null,
                                syncStatus  = SyncStatus.SYNCED
                            )
                        )
                        produceCount++

                        // ProduceListing — skip if one already exists for this record
                        if (produceListingRepository.findByProduceRecordId(record.id!!) != null) continue

                        val listing = produceListingRepository.save(
                            ProduceListingJpaEntity(
                                id = UUID.randomUUID(),
                                produceRecordId = record.id!!,
                                farmerId = farmer.id!!,
                                farmId   = farm.id,
                                agentId  = agent.id!!,
                                lbcId    = lbc.id!!,
                                cropType    = crop.name,
                                cropVariety = crop.variety,
                                grade       = crop.grade,
                                totalQuantityKg     = qty,
                                availableQuantityKg = qty,
                                pricePerKg = crop.price,
                                status   = ProduceListingStatus.AVAILABLE,
                                region   = lbc.region,
                                district = lbc.district,
                                agentName  = "${agent.firstName} ${agent.lastName}",
                                farmerName = "$fFirst $fLast",
                                lbcName    = lbc.name,
                                photosRaw  = null,
                                collectedAt = record.collectedAt
                            )
                        )
                        listingCount++

                        // Product — skip if already linked to this listing
                        if (productRepository.findByProduceListingId(listing.id!!) != null) continue

                        val categoryId = categoryCache.getOrPut(crop.name) {
                            productCategoryRepository.findByNameIgnoreCase(crop.name)?.id
                                ?: productCategoryRepository.save(
                                    ProductCategoryJpaEntity(
                                        id = UUID.randomUUID(),
                                        name = crop.name,
                                        description = "Fresh ${crop.name} sourced from local farmers"
                                    )
                                ).id!!
                        }

                        val productName = "${crop.name} – ${crop.variety}"
                        val location    = "${lbc.district}, ${lbc.region}"
                        val description = "Fresh ${crop.name} from $fFirst $fLast. $location. Grade: ${crop.grade}"

                        productRepository.save(
                            ProductJpaEntity(
                                id = UUID.randomUUID(),
                                categoryId = categoryId,
                                produceListingId = listing.id!!,
                                farmerId = farmer.id!!,
                                agentId  = agent.id!!,
                                name = productName,
                                description = description,
                                pricePerUnit = crop.price,
                                unit = "kg",
                                stockQuantity = qty.toInt(),
                                isAvailable = true,
                                imageUrl = null,
                                isOnDeal = false,
                                isFeatured = slot == 1 && globalFarmerIdx % 5 == 0,
                                originalPrice = null,
                                farmerName = "$fFirst $fLast",
                                location = location,
                                freshnessLabel = "Grade ${crop.grade}",
                                averageRating = BigDecimal.ZERO,
                                reviewCount = 0
                            )
                        )
                        productCount++
                    }
                }
            }
        }

        log.info(
            "LbcHierarchySeeder done — agents:{} farmers:{} farms:{} produce:{} listings:{} products:{}",
            agentCount, farmerCount, farmCount, produceCount, listingCount, productCount
        )
    }
}
