package com.example.service

import com.example.domain.exception.EntityNotFoundException
import com.example.domain.exception.InsufficientStockException
import com.example.domain.messaging.EventProducer
import com.example.domain.model.*
import com.example.domain.repository.*
import com.example.domain.service.OrderRequest
import com.example.domain.service.OrderService

class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val auditLogRepository: AuditLogRepository,
    private val transactionManager: TransactionManager,
    private val eventProducer: EventProducer
) : OrderService {

    override suspend fun createOrder(userId: Long, items: List<OrderRequest>): Order {
        if (items.isEmpty()) throw IllegalArgumentException("Order items cannot be empty")
        
        val order = transactionManager.runInTransaction {
            var total = 0.0
            val orderItems = mutableListOf<OrderItem>()
            
            for (req in items) {
                val product = productRepository.findById(req.productId)
                    ?: throw EntityNotFoundException("Product ${req.productId} not found")
                    
                if (product.stock < req.quantity) {
                    throw InsufficientStockException("Not enough stock for product ${product.name}")
                }
                
                productRepository.decreaseStock(product.id, req.quantity)
                val price = product.price * req.quantity
                total += price
                
                orderItems.add(OrderItem(productId = product.id, quantity = req.quantity, price = price, orderId = 0))
            }
            
            val newOrder = orderRepository.createOrder(
                Order(userId = userId, status = OrderStatus.PENDING, totalPrice = total),
                orderItems
            )
            
            auditLogRepository.create(AuditLog(action = "ORDER_CREATED", details = "User $userId created order ${newOrder.id}"))
            
            newOrder
        }
        
        eventProducer.publishOrderCreated(order.id, userId)
        return order
    }

    override suspend fun cancelOrder(orderId: Long, userId: Long) {
        transactionManager.runInTransaction {
            val order = orderRepository.findById(orderId) 
                ?: throw EntityNotFoundException("Order $orderId not found")
                
            if (order.userId != userId) throw EntityNotFoundException("Order $orderId not found for user")
            if (order.status == OrderStatus.CANCELLED) return@runInTransaction
            
            orderRepository.updateStatus(orderId, OrderStatus.CANCELLED)
            auditLogRepository.create(AuditLog(action = "ORDER_CANCELLED", details = "User $userId cancelled order $orderId"))
        }
    }

    override suspend fun getUserOrders(userId: Long): List<Order> {
        return orderRepository.findByUserId(userId)
    }

    override suspend fun getStats(): Map<String, Any> {
        // Simplified statistic logic for admin
        return mapOf("status" to "ok") // Add real logic if needed
    }
}
