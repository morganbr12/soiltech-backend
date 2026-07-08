package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.CustomerProfile
import com.soiltech.backend.domain.enum.CustomerAccountType
import com.soiltech.backend.domain.enum.CustomerStatus
import com.soiltech.backend.domain.enum.CustomerTier
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "customer_profiles",
    indexes = [
        Index(name = "idx_customer_profiles_user_id", columnList = "userId", unique = true),
        Index(name = "idx_customer_profiles_customer_code", columnList = "customerCode"),
        Index(name = "idx_customer_profiles_status", columnList = "status"),
        Index(name = "idx_customer_profiles_tier", columnList = "tier")
    ]
)
class CustomerProfileJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val userId: UUID,

    @Column(unique = true, length = 20)
    var customerCode: String? = null,

    @Column(length = 100)
    var firstName: String? = null,

    @Column(length = 100)
    var lastName: String? = null,

    @Column(nullable = false, length = 255)
    var fullName: String,

    @Column(length = 255)
    var email: String? = null,

    @Column(length = 20)
    var phone: String? = null,

    @Column(length = 100)
    var region: String? = null,

    @Column(length = 100)
    var district: String? = null,

    @Column(length = 500)
    var address: String? = null,

    @Column(length = 1000)
    var profileImageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(30)")
    var accountType: CustomerAccountType = CustomerAccountType.INDIVIDUAL,

    @Column(length = 255)
    var location: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    var status: CustomerStatus? = null,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    var tier: CustomerTier? = null,

    @Column(length = 255)
    var businessName: String? = null,

    @Column(length = 100)
    var businessType: String? = null,

    @Column(length = 100, unique = true)
    var nationalId: String? = null,

    var isVerified: Boolean? = null,

    var verifiedDate: LocalDateTime? = null,

    @Column(length = 1000)
    var rejectionReason: String? = null,

    var rating: Double? = null,

    var lat: Double? = null,
    var lng: Double? = null,

    var joinedDate: LocalDateTime? = null

) : BaseJpaEntity() {

    fun toDomain(): CustomerProfile = CustomerProfile(
        id = id!!,
        userId = userId,
        customerCode = customerCode,
        firstName = firstName,
        lastName = lastName,
        fullName = fullName,
        email = email,
        phone = phone,
        region = region,
        district = district,
        address = address,
        profileImageUrl = profileImageUrl,
        accountType = accountType,
        location = location,
        status = status ?: CustomerStatus.PENDING,
        tier = tier ?: CustomerTier.BRONZE,
        businessName = businessName,
        businessType = businessType,
        nationalId = nationalId,
        isVerified = isVerified ?: false,
        verifiedDate = verifiedDate,
        rejectionReason = rejectionReason,
        rating = rating ?: 0.0,
        lat = lat,
        lng = lng,
        joinedDate = joinedDate ?: createdAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(profile: CustomerProfile): CustomerProfileJpaEntity = CustomerProfileJpaEntity(
            id = profile.id,
            userId = profile.userId,
            customerCode = profile.customerCode,
            firstName = profile.firstName,
            lastName = profile.lastName,
            fullName = profile.fullName,
            email = profile.email,
            phone = profile.phone,
            region = profile.region,
            district = profile.district,
            address = profile.address,
            profileImageUrl = profile.profileImageUrl,
            accountType = profile.accountType,
            location = profile.location,
            status = profile.status,
            tier = profile.tier,
            businessName = profile.businessName,
            businessType = profile.businessType,
            nationalId = profile.nationalId,
            isVerified = profile.isVerified,
            verifiedDate = profile.verifiedDate,
            rejectionReason = profile.rejectionReason,
            rating = profile.rating,
            lat = profile.lat,
            lng = profile.lng,
            joinedDate = profile.joinedDate
        )
    }
}
