package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.CustomerProfile
import com.soiltech.backend.domain.enum.CustomerAccountType
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "customer_profiles",
    indexes = [Index(name = "idx_customer_profiles_user_id", columnList = "userId", unique = true)]
)
class CustomerProfileJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false, length = 255)
    var fullName: String,

    @Column(length = 20)
    var phone: String? = null,

    @Column(length = 500)
    var address: String? = null,

    @Column(length = 1000)
    var profileImageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    var accountType: CustomerAccountType = CustomerAccountType.INDIVIDUAL,

    @Column(length = 255)
    var location: String? = null
) : BaseJpaEntity() {


    fun toDomain(): CustomerProfile = CustomerProfile(
        id = id!!,
        userId = userId,
        fullName = fullName,
        phone = phone,
        address = address,
        profileImageUrl = profileImageUrl,
        accountType = accountType,
        location = location,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(profile: CustomerProfile): CustomerProfileJpaEntity = CustomerProfileJpaEntity(
            id = profile.id,
            userId = profile.userId,
            fullName = profile.fullName,
            phone = profile.phone,
            address = profile.address,
            profileImageUrl = profile.profileImageUrl,
            accountType = profile.accountType,
            location = profile.location
        )
    }
}
