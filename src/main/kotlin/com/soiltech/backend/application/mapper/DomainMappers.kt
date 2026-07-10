package com.soiltech.backend.application.mapper

import com.soiltech.backend.application.dto.customer.CustomerProfileDto
import com.soiltech.backend.application.dto.farm.FarmDto
import com.soiltech.backend.application.dto.logistics.PickupRequestDto
import com.soiltech.backend.application.dto.order.CustomerOrderDto
import com.soiltech.backend.application.dto.order.CustomerOrderListDto
import com.soiltech.backend.application.dto.order.OrderItemDto
import com.soiltech.backend.application.dto.order.OrderTimelineDto
import com.soiltech.backend.application.dto.payment.PaymentRecordDto
import com.soiltech.backend.application.dto.produce.ProduceListingDto
import com.soiltech.backend.application.dto.produce.ProduceRecordDto
import com.soiltech.backend.application.dto.product.ProductCategoryDto
import com.soiltech.backend.application.dto.product.ProductDto
import com.soiltech.backend.application.dto.product.ProductReviewDto
import com.soiltech.backend.domain.entity.*

fun Farm.toDto() = FarmDto(
    id = id, farmerId = farmerId, name = name, sizeHectares = sizeHectares,
    cropType = cropType, location = location, latitude = latitude,
    longitude = longitude, estimatedYieldKg = estimatedYieldKg,
    lastHarvestDate = lastHarvestDate, photos = photos,
    createdAt = createdAt, updatedAt = updatedAt
)

fun ProduceRecord.toDto(listingStatus: com.soiltech.backend.domain.enum.ProduceListingStatus? = null) = ProduceRecordDto(
    id = id, farmerId = farmerId, farmId = farmId, agentId = agentId,
    cropType = cropType, cropVariety = cropVariety, grade = grade,
    quantityKg = quantityKg, pricePerKg = pricePerKg,
    totalAmount = totalAmount, status = status, listingStatus = listingStatus,
    collectedAt = collectedAt, notes = notes, photos = photos,
    syncStatus = syncStatus, createdAt = createdAt, updatedAt = updatedAt
)

fun ProduceListing.toDto() = ProduceListingDto(
    id = id, produceRecordId = produceRecordId, cropType = cropType,
    cropVariety = cropVariety, grade = grade, totalQuantityKg = totalQuantityKg,
    availableQuantityKg = availableQuantityKg, pricePerKg = pricePerKg,
    status = status, region = region, district = district,
    agentName = agentName, farmerName = farmerName, lbcName = lbcName,
    photos = photos, collectedAt = collectedAt, createdAt = createdAt, updatedAt = updatedAt
)

fun PickupRequest.toDto() = PickupRequestDto(
    id = id, farmerId = farmerId, agentId = agentId, produceRecordId = produceRecordId,
    scheduledDate = scheduledDate, status = status, notes = notes,
    createdAt = createdAt, updatedAt = updatedAt
)

fun PaymentRecord.toDto() = PaymentRecordDto(
    id = id, farmerId = farmerId, agentId = agentId, produceRecordId = produceRecordId,
    amount = amount, currency = currency, status = status, reference = reference,
    paidAt = paidAt, createdAt = createdAt, updatedAt = updatedAt
)

fun Product.toDto() = ProductDto(
    id = id, categoryId = categoryId, produceListingId = produceListingId,
    farmerId = farmerId, agentId = agentId,
    name = name, description = description,
    pricePerUnit = pricePerUnit, unit = unit, stockQuantity = stockQuantity,
    isAvailable = isAvailable, imageUrl = imageUrl,
    isOnDeal = isOnDeal, isFeatured = isFeatured, originalPrice = originalPrice,
    farmerName = farmerName, location = location, freshnessLabel = freshnessLabel,
    averageRating = averageRating, reviewCount = reviewCount,
    createdAt = createdAt, updatedAt = updatedAt
)

fun ProductCategory.toDto() = ProductCategoryDto(
    id = id, name = name, description = description, createdAt = createdAt
)

fun ProductReview.toDto() = ProductReviewDto(
    id = id, productId = productId, customerId = customerId, rating = rating,
    comment = comment, createdAt = createdAt
)

fun OrderItem.toDto() = OrderItemDto(
    id = id, productId = productId, productName = productName,
    agentName = agentName, region = region,
    quantity = quantity, unitPrice = unitPrice, subtotal = subtotal
)

fun OrderTimeline.toDto() = OrderTimelineDto(
    id = id, status = status, note = note, createdAt = createdAt, createdBy = createdBy
)

fun CustomerOrder.toDto(items: List<OrderItem>, timeline: List<OrderTimeline>) = CustomerOrderDto(
    id = id, customerId = customerId, customerName = customerName,
    status = status, totalAmount = totalAmount,
    deliveryAddress = deliveryAddress, paymentType = paymentType, notes = notes,
    items = items.map { it.toDto() },
    timeline = timeline.map { it.toDto() },
    createdAt = createdAt, updatedAt = updatedAt
)

fun CustomerOrder.toListDto(itemCount: Int) = CustomerOrderListDto(
    id = id, customerId = customerId, customerName = customerName,
    status = status, totalAmount = totalAmount,
    deliveryAddress = deliveryAddress, paymentType = paymentType,
    itemCount = itemCount, createdAt = createdAt, updatedAt = updatedAt
)

fun CustomerProfile.toDto() = CustomerProfileDto(
    id = id, userId = userId, fullName = fullName, phone = phone,
    address = address, profileImageUrl = profileImageUrl,
    accountType = accountType, location = location,
    createdAt = createdAt, updatedAt = updatedAt
)

