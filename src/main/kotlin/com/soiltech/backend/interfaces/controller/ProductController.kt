package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.product.CreateProductReviewRequest
import com.soiltech.backend.application.dto.product.ProductCategoryDto
import com.soiltech.backend.application.dto.product.ProductDto
import com.soiltech.backend.application.dto.product.ProductReviewDto
import com.soiltech.backend.application.usecase.product.CreateProductReviewUseCase
import com.soiltech.backend.application.usecase.product.GetProductUseCase
import com.soiltech.backend.application.usecase.product.ListDealsUseCase
import com.soiltech.backend.application.usecase.product.ListFeaturedProductsUseCase
import com.soiltech.backend.application.usecase.product.ListProductCategoriesUseCase
import com.soiltech.backend.application.usecase.product.ListProductReviewsUseCase
import com.soiltech.backend.application.usecase.product.ListProductsUseCase
import com.soiltech.backend.domain.repository.ProductRepository
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/products")
class ProductController(
    private val listProductsUseCase: ListProductsUseCase,
    private val listDealsUseCase: ListDealsUseCase,
    private val listFeaturedProductsUseCase: ListFeaturedProductsUseCase,
    private val getProductUseCase: GetProductUseCase,
    private val listProductReviewsUseCase: ListProductReviewsUseCase,
    private val createProductReviewUseCase: CreateProductReviewUseCase,
    private val productRepository: ProductRepository
) {

    @GetMapping("/deals")
    fun listDeals(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ): ResponseEntity<ApiResponse<List<ProductDto>>> {
        val (products, meta) = listDealsUseCase.execute(page, perPage)
        return ResponseEntity.ok(ApiResponse.success(products, meta = meta))
    }

    @GetMapping("/featured")
    fun listFeatured(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ): ResponseEntity<ApiResponse<List<ProductDto>>> {
        val (products, meta) = listFeaturedProductsUseCase.execute(page, perPage)
        return ResponseEntity.ok(ApiResponse.success(products, meta = meta))
    }

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int,
        @RequestParam(name = "category_id", required = false) categoryId: UUID?,
        @RequestParam(required = false) query: String?
    ): ResponseEntity<ApiResponse<List<ProductDto>>> {
        val (products, meta) = listProductsUseCase.execute(categoryId, query, page, perPage)
        return ResponseEntity.ok(ApiResponse.success(products, meta = meta))
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponse<ProductDto>> {
        val data = getProductUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @GetMapping("/{id}/reviews")
    fun listReviews(
        @PathVariable id: UUID,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ): ResponseEntity<ApiResponse<List<ProductReviewDto>>> {
        val (reviews, meta) = listProductReviewsUseCase.execute(id, page, perPage)
        return ResponseEntity.ok(ApiResponse.success(reviews, meta = meta))
    }

    @PostMapping("/backfill-farmer-agent")
    @PreAuthorize("hasRole('ADMIN')")
    fun backfillFarmerAgent(): ResponseEntity<ApiResponse<Map<String, Int>>> {
        val updated = productRepository.backfillFarmerAgentIds()
        return ResponseEntity.ok(ApiResponse.success(mapOf("updated" to updated), "Backfill complete"))
    }

    @PostMapping("/{id}/reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    fun createReview(
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateProductReviewRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<ProductReviewDto>> {
        val data = createProductReviewUseCase.execute(id, request, principal.id)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Review submitted"))
    }
}

@RestController
@RequestMapping("/product-categories")
class ProductCategoryController(
    private val listProductCategoriesUseCase: ListProductCategoriesUseCase
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "50") perPage: Int
    ): ResponseEntity<ApiResponse<List<ProductCategoryDto>>> {
        val (categories, meta) = listProductCategoriesUseCase.execute(page, perPage)
        return ResponseEntity.ok(ApiResponse.success(categories, meta = meta))
    }
}
