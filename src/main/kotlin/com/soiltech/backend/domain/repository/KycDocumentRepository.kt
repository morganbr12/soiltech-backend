package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.KycDocument
import java.util.UUID

interface KycDocumentRepository {
    fun findByCustomerId(customerId: UUID): List<KycDocument>
    fun save(document: KycDocument): KycDocument
}
