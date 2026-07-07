package com.soiltech.backend.interfaces.response

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.domain.Page

data class PaginationMeta(
    val total: Int,
    val page: Int,
    @JsonProperty("per_page")
    val perPage: Int,
    @JsonProperty("last_page")
    val lastPage: Int
) {
    companion object {
        fun <T> from(page: Page<T>, currentPage: Int, perPage: Int): PaginationMeta = PaginationMeta(
            total = page.totalElements.toInt(),
            page = currentPage,
            perPage = perPage,
            lastPage = page.totalPages
        )
    }
}
