package com.ecommerce.product.service

import com.ecommerce.product.dto.ProductResponse
import com.ecommerce.product.dto.ProductSearchRequest
import com.ecommerce.product.dto.RegisterProductRequest
import com.ecommerce.product.dto.UpdateProductRequest
import com.ecommerce.product.entity.Product
import com.ecommerce.product.entity.ProductImage
import com.ecommerce.product.repository.CategoryRepository
import com.ecommerce.product.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
  private val productRepository: ProductRepository,
  private val categoryRepository: CategoryRepository
) {

  @Transactional
  fun registerProduct(request: RegisterProductRequest): ProductResponse {
    val category = categoryRepository.findByIdAndNotDeleted(request.categoryId)
      ?: throw IllegalArgumentException("존재하지 않는 카테고리입니다: ${request.categoryId}")

    val product = Product(
      name = request.name,
      description = request.description,
      category = category,
      originalPrice = request.originalPrice,
      salePrice = request.salePrice,
      status = request.status
    )

    request.images.forEach { imageData ->
      val productImage = ProductImage(
        product = product,
        imageUrl = imageData.imageUrl,
        displayOrder = imageData.displayOrder,
        isThumbnail = imageData.isThumbnail
      )
      product.addImage(productImage)
    }

    val savedProduct = productRepository.save(product)

    return ProductResponse.from(savedProduct)
  }

  @Transactional(readOnly = true)
  fun getProduct(productId: Long): ProductResponse {
    val product = productRepository.findByIdAndNotDeleted(productId)
      ?: throw IllegalArgumentException("존재하지 않는 상품입니다: $productId")

    // FETCH JOIN으로 모든 연관 엔티티가 로딩되어 있으므로 안전하게 변환
    return ProductResponse.from(product)
  }

  @Transactional(readOnly = true)
  fun getAllProducts(): List<ProductResponse> {
    return productRepository.findAllNotDeleted()
      .map { ProductResponse.from(it) }
  }

  @Transactional(readOnly = true)
  fun searchProducts(request: ProductSearchRequest): List<ProductResponse> {
    return productRepository.searchProducts(
      categoryId = request.categoryId,
      keyword = request.keyword,
      minPrice = request.minPrice,
      maxPrice = request.maxPrice,
      status = request.status
    ).map { ProductResponse.from(it) }
  }

  @Transactional
  fun updateProduct(productId: Long, request: UpdateProductRequest): ProductResponse {
    val product = productRepository.findByIdAndNotDeleted(productId)
      ?: throw IllegalArgumentException("존재하지 않는 상품입니다: $productId")

    if (request.categoryId != null && request.categoryId != product.category.id) {
      val newCategory = categoryRepository.findByIdAndNotDeleted(request.categoryId)
        ?: throw IllegalArgumentException("존재하지 않는 카테고리입니다: ${request.categoryId}")
      product.changeCategory(newCategory)
    }

    request.name?.let { product.updateName(it) }
    request.description?.let { product.updateDescription(it) }

    if (request.originalPrice != null || request.salePrice != null) {
      val newOriginalPrice = request.originalPrice ?: product.originalPrice
      val newSalePrice = request.salePrice ?: product.salePrice
      product.updatePrice(newOriginalPrice, newSalePrice)
    }

    request.status?.let { product.changeStatus(it) }

    return ProductResponse.from(product)
  }

  @Transactional
  fun deleteProduct(productId: Long) {
    val product = productRepository.findByIdAndNotDeleted(productId)
      ?: throw IllegalArgumentException("존재하지 않는 상품입니다: $productId")

    product.delete()
  }

  @Transactional(readOnly = true)
  fun getProductStatsByCategory() = productRepository.getProductStatsByCategory()
}