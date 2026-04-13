package com.example.service

import com.example.domain.exception.EntityNotFoundException
import com.example.domain.exception.InsufficientStockException
import com.example.domain.messaging.EventProducer
import com.example.domain.model.*
import com.example.domain.repository.*
import com.example.domain.service.OrderRequest
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val productRepository = mockk<ProductRepository>()
    private val auditLogRepository = mockk<AuditLogRepository>()
    private val transactionManager = mockk<TransactionManager>()
    private val eventProducer = mockk<EventProducer>(relaxed = true)

    private val orderService = OrderServiceImpl(
        orderRepository, productRepository, auditLogRepository, transactionManager, eventProducer
    )

    init {
        // TransactionManager just executes the block directly in tests
        coEvery { transactionManager.runInTransaction(any<suspend () -> Any>()) } coAnswers {
            val block = firstArg<suspend () -> Any>()
            block()
        }
    }

    @Test
    fun `createOrder should throw if items list is empty`() = runBlocking {
        assertThrows<IllegalArgumentException> {
            orderService.createOrder(1, emptyList())
        }
    }

    @Test
    fun `createOrder should throw if product not found`() = runBlocking {
        coEvery { productRepository.findById(99) } returns null

        assertThrows<EntityNotFoundException> {
            orderService.createOrder(1, listOf(OrderRequest(99, 1)))
        }
    }

    @Test
    fun `createOrder should throw if insufficient stock`() = runBlocking {
        val product = Product(1, "Laptop", "Test", 1000.0, 2)
        coEvery { productRepository.findById(1) } returns product

        assertThrows<InsufficientStockException> {
            orderService.createOrder(1, listOf(OrderRequest(1, 5)))
        }
    }

    @Test
    fun `createOrder should calculate total and decrease stock`() = runBlocking {
        val product = Product(1, "Laptop", "Test", 500.0, 10)
        coEvery { productRepository.findById(1) } returns product
        coEvery { productRepository.decreaseStock(1, 3) } returns true
        coEvery { orderRepository.createOrder(any(), any()) } answers {
            firstArg<Order>().copy(id = 42)
        }
        coEvery { auditLogRepository.create(any()) } answers { firstArg() }

        val order = orderService.createOrder(1, listOf(OrderRequest(1, 3)))

        assertEquals(42, order.id)
        assertEquals(1500.0, order.totalPrice) // 500 * 3
        coVerify { productRepository.decreaseStock(1, 3) }
        coVerify { eventProducer.publishOrderCreated(42, 1) }
    }

    @Test
    fun `createOrder with multiple items should sum total`() = runBlocking {
        val laptop = Product(1, "Laptop", "Test", 1000.0, 10)
        val mouse = Product(2, "Mouse", "Test", 50.0, 100)
        coEvery { productRepository.findById(1) } returns laptop
        coEvery { productRepository.findById(2) } returns mouse
        coEvery { productRepository.decreaseStock(any(), any()) } returns true
        coEvery { orderRepository.createOrder(any(), any()) } answers {
            firstArg<Order>().copy(id = 10)
        }
        coEvery { auditLogRepository.create(any()) } answers { firstArg() }

        val order = orderService.createOrder(1, listOf(
            OrderRequest(1, 2),  // 1000 * 2 = 2000
            OrderRequest(2, 3)   // 50 * 3 = 150
        ))

        assertEquals(2150.0, order.totalPrice)
    }

    @Test
    fun `cancelOrder should throw if order not found`() = runBlocking {
        coEvery { orderRepository.findById(99) } returns null

        assertThrows<EntityNotFoundException> {
            orderService.cancelOrder(99, 1)
        }
    }

    @Test
    fun `cancelOrder should throw if order belongs to different user`() = runBlocking {
        val order = Order(1, userId = 5, status = OrderStatus.PENDING, totalPrice = 100.0)
        coEvery { orderRepository.findById(1) } returns order

        assertThrows<EntityNotFoundException> {
            orderService.cancelOrder(1, 999) // wrong userId
        }
    }

    @Test
    fun `cancelOrder should skip if already cancelled`() = runBlocking {
        val order = Order(1, userId = 1, status = OrderStatus.CANCELLED, totalPrice = 100.0)
        coEvery { orderRepository.findById(1) } returns order

        orderService.cancelOrder(1, 1)

        coVerify(exactly = 0) { orderRepository.updateStatus(any(), any()) }
    }

    @Test
    fun `cancelOrder should update status and create audit log`() = runBlocking {
        val order = Order(1, userId = 1, status = OrderStatus.PENDING, totalPrice = 100.0)
        coEvery { orderRepository.findById(1) } returns order
        coEvery { orderRepository.updateStatus(1, OrderStatus.CANCELLED) } returns true
        coEvery { auditLogRepository.create(any()) } answers { firstArg() }

        orderService.cancelOrder(1, 1)

        coVerify { orderRepository.updateStatus(1, OrderStatus.CANCELLED) }
        coVerify { auditLogRepository.create(match { it.action == "ORDER_CANCELLED" }) }
    }

    @Test
    fun `getUserOrders should return orders by userId`() = runBlocking {
        val orders = listOf(
            Order(1, userId = 5, status = OrderStatus.PENDING, totalPrice = 100.0),
            Order(2, userId = 5, status = OrderStatus.COMPLETED, totalPrice = 200.0)
        )
        coEvery { orderRepository.findByUserId(5) } returns orders

        val result = orderService.getUserOrders(5)

        assertEquals(2, result.size)
    }
}
