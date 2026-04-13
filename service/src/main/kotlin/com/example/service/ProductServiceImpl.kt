package com.example.service

import com.example.domain.cache.ProductCache
import com.example.domain.exception.EntityNotFoundException
import com.example.domain.model.Product
import com.example.domain.repository.ProductRepository
import com.example.domain.service.ProductService

class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val productCache: ProductCache
) : ProductService {

    override suspend fun addProduct(name: String, description: String, price: Double, stock: Int): Product {
        return productRepository.create(Product(name = name, description = description, price = price, stock = stock))
    }

    override suspend fun updateProduct(id: Long, name: String, description: String, price: Double, stock: Int): Product {
        val product = Product(id = id, name = name, description = description, price = price, stock = stock)
        if (!productRepository.update(id, product)) {
            throw EntityNotFoundException("Product $id not found")
        }
        productCache.invalidate(id)
        return product
    }

    override suspend fun deleteProduct(id: Long) {
        if (!productRepository.delete(id)) {
            throw EntityNotFoundException("Product $id not found")
        }
        productCache.invalidate(id)
    }

    override suspend fun getProducts(): List<Product> {
        return productRepository.findAll()
    }

    override suspend fun getProduct(id: Long): Product {
        val cached = productCache.getProduct(id)
        if (cached != null) return cached
        
        val product = productRepository.findById(id) ?: throw EntityNotFoundException("Product $id not found")
        productCache.setProduct(product)
        return product
    }
}
