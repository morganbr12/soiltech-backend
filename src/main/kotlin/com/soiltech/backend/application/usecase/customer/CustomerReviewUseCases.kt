package com.soiltech.backend.application.usecase.customer

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.domain.enum.ReviewStatus
import com.soiltech.backend.domain.enum.ReviewTargetType
import com.soiltech.backend.domain.repository.CustomerReviewRepository
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class ListReviewsUseCase(private val reviewRepository: CustomerReviewRepository) {
    fun execute(
        status: ReviewStatus?,
        targetType: ReviewTargetType?,
        region: String?,
        search: String?,
        page: Int,
        limit: Int,
        sortBy: String,
        sortOrder: String
    ): Triple<List<ReviewResponse>, ReviewSummaryResponse, PaginationMeta> {
        val direction = if (sortOrder.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val sortField = when (sortBy) { "rating" -> "rating"; else -> "createdAt" }
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), limit.coerceIn(1, 100), Sort.by(direction, sortField))

        val resultPage = reviewRepository.findAll(status, targetType, region, search, pageable)
        val counts = reviewRepository.countByStatus()
        val avg = reviewRepository.avgRating()

        val summary = ReviewSummaryResponse(
            total = counts.values.sumOf { it },
            approved = counts.getOrDefault(ReviewStatus.APPROVED, 0L),
            pending = counts.getOrDefault(ReviewStatus.PENDING, 0L),
            flagged = counts.getOrDefault(ReviewStatus.FLAGGED, 0L),
            rejected = counts.getOrDefault(ReviewStatus.REJECTED, 0L),
            avgRating = avg
        )
        return Triple(resultPage.content.map { it.toResponse() }, summary, PaginationMeta.from(resultPage, page, limit))
    }
}

@Service
class ApproveReviewUseCase(private val reviewRepository: CustomerReviewRepository) {
    @Transactional
    fun execute(id: UUID): ReviewResponse {
        val review = reviewRepository.findById(id) ?: throw NotFoundException("Review not found with id: $id")
        if (review.status == ReviewStatus.APPROVED) throw BadRequestException("Review is already approved")
        return reviewRepository.save(review.copy(status = ReviewStatus.APPROVED, flagReason = null, updatedAt = LocalDateTime.now())).toResponse()
    }
}

@Service
class FlagReviewUseCase(private val reviewRepository: CustomerReviewRepository) {
    @Transactional
    fun execute(id: UUID, request: FlagReviewRequest): ReviewResponse {
        val review = reviewRepository.findById(id) ?: throw NotFoundException("Review not found with id: $id")
        if (review.status == ReviewStatus.FLAGGED) throw BadRequestException("Review is already flagged")
        return reviewRepository.save(review.copy(status = ReviewStatus.FLAGGED, flagReason = request.reason, updatedAt = LocalDateTime.now())).toResponse()
    }
}

@Service
class DeleteReviewUseCase(private val reviewRepository: CustomerReviewRepository) {
    @Transactional
    fun execute(id: UUID) {
        reviewRepository.findById(id) ?: throw NotFoundException("Review not found with id: $id")
        reviewRepository.delete(id)
    }
}

// ── Mapper ────────────────────────────────────────────────────────────────────

private fun com.soiltech.backend.domain.entity.CustomerReview.toResponse() = ReviewResponse(
    id = id, customerId = customerId, customerName = customerName,
    targetType = targetType, targetId = targetId, targetName = targetName,
    rating = rating, comment = comment, status = status, region = region, createdAt = createdAt
)
