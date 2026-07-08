package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.application.usecase.customer.*
import com.soiltech.backend.domain.enum.ReviewStatus
import com.soiltech.backend.domain.enum.ReviewTargetType
import com.soiltech.backend.interfaces.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/customers/reviews")
class CustomerReviewAdminController(
    private val listReviewsUseCase: ListReviewsUseCase,
    private val approveReviewUseCase: ApproveReviewUseCase,
    private val flagReviewUseCase: FlagReviewUseCase,
    private val deleteReviewUseCase: DeleteReviewUseCase
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:reviews')")
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) targetType: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(name = "sortBy", defaultValue = "createdAt") sortBy: String,
        @RequestParam(name = "sortOrder", defaultValue = "desc") sortOrder: String
    ): ResponseEntity<ApiResponse<List<ReviewResponse>>> {
        val reviewStatus = status?.let { ReviewStatus.fromValue(it) }
        val reviewTargetType = targetType?.let { ReviewTargetType.fromValue(it) }
        val (items, summary, meta) = listReviewsUseCase.execute(
            reviewStatus, reviewTargetType, region, search, page, limit, sortBy, sortOrder
        )
        return ResponseEntity.ok(ApiResponse.success(items, meta = meta, summary = summary))
    }

    @PatchMapping("/{reviewId}/approve")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:reviews')")
    fun approve(@PathVariable reviewId: UUID): ResponseEntity<ApiResponse<ReviewResponse>> {
        val data = approveReviewUseCase.execute(reviewId)
        return ResponseEntity.ok(ApiResponse.success(data, "Review approved"))
    }

    @PatchMapping("/{reviewId}/flag")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:reviews')")
    fun flag(
        @PathVariable reviewId: UUID,
        @RequestBody(required = false) request: FlagReviewRequest?
    ): ResponseEntity<ApiResponse<ReviewResponse>> {
        val data = flagReviewUseCase.execute(reviewId, request ?: FlagReviewRequest())
        return ResponseEntity.ok(ApiResponse.success(data, "Review flagged"))
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:reviews')")
    fun delete(@PathVariable reviewId: UUID): ResponseEntity<ApiResponse<Unit?>> {
        deleteReviewUseCase.execute(reviewId)
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted"))
    }
}
