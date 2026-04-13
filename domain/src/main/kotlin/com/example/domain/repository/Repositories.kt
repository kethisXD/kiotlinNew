package com.example.domain.repository

import com.example.domain.model.*

interface UserRepository {
    suspend fun create(user: User): User
    suspend fun findByUsername(username: String): User?
    suspend fun findById(id: Long): User?
}

interface ProductRepository {
    suspend fun create(product: Product): Product
    suspend fun update(id: Long, product: Product): Boolean
    suspend fun delete(id: Long): Boolean
    suspend fun findById(id: Long): Product?
    suspend fun findAll(): List<Product>
    suspend fun decreaseStock(id: Long, amount: Int): Boolean
}

interface OrderRepository {
    suspend fun createOrder(order: Order, items: List<OrderItem>): Order
    suspend fun findById(id: Long): Order?
    suspend fun findByUserId(userId: Long): List<Order>
    suspend fun delete(id: Long): Boolean
    suspend fun updateStatus(id: Long, status: OrderStatus): Boolean
}

interface AuditLogRepository {
    suspend fun create(log: AuditLog): AuditLog
}
