package com.example.domain.cache

import com.example.domain.model.Product

interface ProductCache {
    suspend fun getProduct(id: Long): Product?
    suspend fun setProduct(product: Product, ttlSeconds: Long = 3600)
    suspend fun invalidate(id: Long)
}
