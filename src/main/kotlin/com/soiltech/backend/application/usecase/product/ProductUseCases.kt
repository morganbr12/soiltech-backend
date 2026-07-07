package com.soiltech.backend.application.usecase.product

import com.soiltech.backend.application.dto.product.ProductCategoryDto
import com.soiltech.backend.application.dto.product.ProductDto
import com.soiltech.backend.application.dto.product.ProductReviewDto
import com.soiltech.backend.application.dto.product.CreateProductReviewRequest
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.domain.entity.ProductReview
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.domain.repository.ProductCategoryRepository
import com.soiltech.backend.domain.repository.ProductRepository
import com.soiltech.backend.domain.repository.ProductReviewRepository
import com.soiltech.backend.infrastructure.persistence.jpa.ProductReviewJpaRepository
import com.soiltech.backend.interfaces.exception.ConflictException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.UUID

@Service
class ListProductsUseCase(
    private val productRepository: ProductRepository
) {
    fun execute(categoryId: UUID?, query: String?, page: Int, perPage: Int): Pair<List<ProductDto>, PaginationMeta> {
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = productRepository.findAll(categoryId, query, pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class ListDealsUseCase(
    private val productRepository: ProductRepository
) {
    fun execute(page: Int, perPage: Int): Pair<List<ProductDto>, PaginationMeta> {
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = productRepository.findDeals(pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class ListFeaturedProductsUseCase(
    private val productRepository: ProductRepository
) {
    fun execute(page: Int, perPage: Int): Pair<List<ProductDto>, PaginationMeta> {
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = productRepository.findFeatured(pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class GetProductUseCase(
    private val productRepository: ProductRepository
) {
    fun execute(productId: UUID): ProductDto {
        return productRepository.findById(productId)?.toDto()
            ?: throw NotFoundException("Product not found")
    }
}

@Service
class ListProductCategoriesUseCase(
    private val productCategoryRepository: ProductCategoryRepository
) {
    fun execute(page: Int, perPage: Int): Pair<List<ProductCategoryDto>, PaginationMeta> {
        val pageable = PageRequest.of(page - 1, perPage)
        val result = productCategoryRepository.findAll(pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class ListProductReviewsUseCase(
    private val productReviewRepository: ProductReviewRepository
) {
    fun execute(productId: UUID, page: Int, perPage: Int): Pair<List<ProductReviewDto>, PaginationMeta> {
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = productReviewRepository.findByProductId(productId, pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class CreateProductReviewUseCase(
    private val productReviewRepository: ProductReviewRepository,
    private val productReviewJpaRepository: ProductReviewJpaRepository,
    private val productRepository: ProductRepository,
    private val customerProfileRepository: CustomerProfileRepository
) {
    @Transactional
    fun execute(productId: UUID, request: CreateProductReviewRequest, userId: UUID): ProductReviewDto {
        val product = productRepository.findById(productId)
            ?: throw NotFoundException("Product not found")
        val customer = customerProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Customer profile not found")

        if (productReviewRepository.existsByProductIdAndCustomerId(productId, customer.id)) {
            throw ConflictException("You have already reviewed this product")
        }

        val review = productReviewRepository.save(
            ProductReview(
                id = UUID.randomUUID(),
                productId = productId,
                customerId = customer.id,
                rating = request.rating.coerceIn(1, 5),
                comment = request.comment,
                createdAt = LocalDateTime.now()
            )
        )

        val newAvg = productReviewJpaRepository.findAverageRatingByProductId(productId)
        val newCount = productReviewJpaRepository.countByProductId(productId)
        productRepository.save(
            product.copy(
                averageRating = BigDecimal(newAvg).setScale(1, RoundingMode.HALF_UP),
                reviewCount = newCount.toInt()
            )
        )

        return review.toDto()
    }
}
