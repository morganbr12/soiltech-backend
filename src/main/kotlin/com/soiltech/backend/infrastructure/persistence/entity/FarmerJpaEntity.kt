package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.enum.FarmerStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "farmers",
    indexes = [
        Index(name = "idx_farmers_agent_id", columnList = "agentId"),
        Index(name = "idx_farmers_lbc_id", columnList = "lbcId"),
        Index(name = "idx_farmers_status", columnList = "status"),
        Index(name = "idx_farmers_region", columnList = "region"),
        Index(name = "idx_farmers_kyc_verified", columnList = "kycVerified"),
        Index(name = "idx_farmers_farmer_code", columnList = "farmerCode", unique = true),
        Index(name = "idx_farmers_phone", columnList = "phone", unique = true)
    ]
)
class FarmerJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false, unique = true, length = 20)
    val farmerCode: String,

    @Column(nullable = false, length = 100)
    var firstName: String,

    @Column(nullable = false, length = 100)
    var lastName: String,

    @Column(nullable = false, unique = true, length = 20)
    var phone: String,

    @Column(length = 255)
    var email: String? = null,

    @Column(unique = true, length = 50)
    var nationalId: String? = null,

    @Column(nullable = false)
    var agentId: UUID,

    @Column(nullable = false)
    var lbcId: UUID,

    @Column(nullable = false, length = 100)
    var region: String,

    @Column(nullable = false, length = 100)
    var district: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    var status: FarmerStatus = FarmerStatus.PENDING,

    @Column(nullable = false)
    var kycVerified: Boolean = false,

    @Column
    var latitude: Double? = null,

    @Column
    var longitude: Double? = null,

    @Column(name = "crop_types", columnDefinition = "text")
    var cropTypesRaw: String? = null,

    @Column(length = 1000)
    var rejectionReason: String? = null,

    @Column(nullable = false)
    var joinedDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false, length = 201)
    var fullName: String = ""

) : BaseJpaEntity() {

    fun toDomain(agentName: String = "", lbcName: String = "") = com.soiltech.backend.domain.entity.Farmer(
        id = id!!,
        farmerCode = farmerCode,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        email = email,
        nationalId = nationalId,
        agentId = agentId,
        agentName = agentName,
        lbcId = lbcId,
        lbcName = lbcName,
        region = region,
        district = district,
        status = status,
        kycVerified = kycVerified,
        latitude = latitude,
        longitude = longitude,
        cropTypes = cropTypesRaw?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
        rejectionReason = rejectionReason,
        joinedDate = joinedDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdBy = createdBy,
        updatedBy = updatedBy
    )
}
