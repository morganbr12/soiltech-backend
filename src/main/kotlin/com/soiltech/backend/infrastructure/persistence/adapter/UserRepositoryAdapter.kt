package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.User
import com.soiltech.backend.domain.repository.UserRepository
import com.soiltech.backend.infrastructure.persistence.entity.UserJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.UserJpaRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserRepositoryAdapter(
    private val jpaRepository: UserJpaRepository
) : UserRepository {

    override fun findById(id: UUID): User? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByEmail(email: String): User? =
        jpaRepository.findByEmail(email)?.toDomain()

    override fun findByPhone(phone: String): User? =
        jpaRepository.findByPhone(phone)?.toDomain()

    override fun save(user: User): User {
        val entity = jpaRepository.findById(user.id).orElse(UserJpaEntity.fromDomain(user))
        entity.apply {
            passwordHash = user.passwordHash
            phone = user.phone
        }
        return jpaRepository.save(entity).toDomain()
    }

    override fun existsByEmail(email: String): Boolean =
        jpaRepository.existsByEmail(email)

    override fun existsByPhone(phone: String): Boolean =
        jpaRepository.existsByPhone(phone)
}
