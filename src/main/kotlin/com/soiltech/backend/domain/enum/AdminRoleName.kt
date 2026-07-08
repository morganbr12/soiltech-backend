package com.soiltech.backend.domain.enum

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class AdminRoleName(@JsonValue val value: String, val label: String) {
    SUPER_ADMIN("super_admin", "Super Admin"),
    OPERATIONS_MANAGER("operations_manager", "Operations Manager"),
    REGIONAL_MANAGER("regional_manager", "Regional Manager"),
    LBC_MANAGER("lbc_manager", "LBC Manager"),
    FINANCE_MANAGER("finance_manager", "Finance Manager"),
    WAREHOUSE_MANAGER("warehouse_manager", "Warehouse Manager"),
    LOGISTICS_MANAGER("logistics_manager", "Logistics Manager"),
    QA_OFFICER("qa_officer", "Quality Assurance Officer"),
    CUSTOMER_SUPPORT("customer_support", "Customer Support"),
    AUDITOR("auditor", "Auditor"),
    ANALYST("analyst", "Read-only Analyst");

    companion object {
        @JsonCreator
        fun fromValue(value: String): AdminRoleName =
            entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown admin role: $value")
    }
}
