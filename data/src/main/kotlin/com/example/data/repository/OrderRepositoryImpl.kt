package com.example.data.repository

import com.example.data.db.OrderItems
import com.example.data.db.Orders
import com.example.domain.model.Order
import com.example.domain.model.OrderItem
import com.example.domain.model.OrderStatus
import com.example.domain.repository.OrderRepository
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class OrderRepositoryImpl : OrderRepository {
    private fun ResultRow.toOrder(): Order = Order(
        id = this[Orders.id],
        userId = this[Orders.userId],
        status = OrderStatus.valueOf(this[Orders.status]),
        totalPrice = this[Orders.total],
        createdAt = this[Orders.createdAt]
    )

    override suspend fun createOrder(order: Order, items: List<OrderItem>): Order = newSuspendedTransaction(Dispatchers.IO) {
        val orderId = Orders.insert {
            it[userId] = order.userId
            it[status] = order.status.name
            it[total] = order.totalPrice
            it[createdAt] = order.createdAt
        } get Orders.id
        
        OrderItems.batchInsert(items) { item ->
            this[OrderItems.orderId] = orderId
            this[OrderItems.productId] = item.productId
            this[OrderItems.quantity] = item.quantity
            this[OrderItems.price] = item.price
        }
        
        order.copy(id = orderId)
    }

    override suspend fun findById(id: Long): Order? = newSuspendedTransaction(Dispatchers.IO) {
        Orders.selectAll().where { Orders.id eq id }
            .singleOrNull()?.toOrder()
    }

    override suspend fun findByUserId(userId: Long): List<Order> = newSuspendedTransaction(Dispatchers.IO) {
        Orders.selectAll().where { Orders.userId eq userId }
            .map { it.toOrder() }
    }

    override suspend fun delete(id: Long): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        OrderItems.deleteWhere { OrderItems.orderId eq id }
        val rows = Orders.deleteWhere { Orders.id eq id }
        rows > 0
    }

    override suspend fun updateStatus(id: Long, status: OrderStatus): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        val rows = Orders.update({ Orders.id eq id }) {
            it[Orders.status] = status.name
        }
        rows > 0
    }
}

