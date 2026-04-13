package com.example.domain.service

import com.example.domain.model.*

data class OrderRequest(val productId: Long, val quantity: Int)

interface OrderService {
    suspend fun createOrder(userId: Long, items: List<OrderRequest>): Order
    suspend fun cancelOrder(orderId: Long, userId: Long)
    suspend fun getUserOrders(userId: Long): List<Order>
    suspend fun getStats(): Map<String, Any>
}

interface ProductService {
    suspend fun addProduct(name: String, description: String, price: Double, stock: Int): Product
    suspend fun updateProduct(id: Long, name: String, description: String, price: Double, stock: Int): Product
    suspend fun deleteProduct(id: Long)
    suspend fun getProducts(): List<Product>
    suspend fun getProduct(id: Long): Product
}
