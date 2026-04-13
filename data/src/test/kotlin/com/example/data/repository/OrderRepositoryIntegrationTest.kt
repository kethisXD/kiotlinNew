package com.example.data.repository

import com.example.data.db.DatabaseFactory
import com.example.domain.model.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Testcontainers
class OrderRepositoryIntegrationTest {

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @BeforeAll
        fun setUpAll() {
            postgresContainer.start()
            DatabaseFactory.init(
                postgresContainer.jdbcUrl,
                postgresContainer.username,
                postgresContainer.password
            )
        }
    }

    private val userRepo = UserRepositoryImpl()
    private val productRepo = ProductRepositoryImpl()
    private val orderRepo = OrderRepositoryImpl()

    @Test
    fun `test create order with items and find by user`() = runBlocking {
        // Create user
        val user = userRepo.create(User(username = "ordertest", passwordHash = "hash", role = Role.USER))
        assertTrue(user.id > 0)

        // Create product
        val product = productRepo.create(Product(name = "TestItem", description = "Desc", price = 99.99, stock = 50))
        assertTrue(product.id > 0)

        // Create order
        val orderItems = listOf(
            OrderItem(orderId = 0, productId = product.id, quantity = 2, price = 199.98)
        )
        val order = orderRepo.createOrder(
            Order(userId = user.id, status = OrderStatus.PENDING, totalPrice = 199.98),
            orderItems
        )
        assertTrue(order.id > 0)

        // Find by id
        val found = orderRepo.findById(order.id)
        assertNotNull(found)
        assertEquals(OrderStatus.PENDING, found!!.status)
        assertEquals(199.98, found.totalPrice)

        // Find by user
        val userOrders = orderRepo.findByUserId(user.id)
        assertEquals(1, userOrders.size)
        assertEquals(order.id, userOrders[0].id)
    }

    @Test
    fun `test update order status`() = runBlocking {
        val user = userRepo.create(User(username = "statustest", passwordHash = "hash", role = Role.USER))
        val product = productRepo.create(Product(name = "StatusItem", description = "D", price = 10.0, stock = 100))

        val order = orderRepo.createOrder(
            Order(userId = user.id, status = OrderStatus.PENDING, totalPrice = 10.0),
            listOf(OrderItem(orderId = 0, productId = product.id, quantity = 1, price = 10.0))
        )

        val updated = orderRepo.updateStatus(order.id, OrderStatus.CANCELLED)
        assertTrue(updated)

        val found = orderRepo.findById(order.id)
        assertEquals(OrderStatus.CANCELLED, found!!.status)
    }

    @Test
    fun `test delete order with cascade`() = runBlocking {
        val user = userRepo.create(User(username = "deletetest", passwordHash = "hash", role = Role.USER))
        val product = productRepo.create(Product(name = "DelItem", description = "D", price = 5.0, stock = 100))

        val order = orderRepo.createOrder(
            Order(userId = user.id, status = OrderStatus.PENDING, totalPrice = 5.0),
            listOf(OrderItem(orderId = 0, productId = product.id, quantity = 1, price = 5.0))
        )

        val deleted = orderRepo.delete(order.id)
        assertTrue(deleted)

        val found = orderRepo.findById(order.id)
        assertNull(found)
    }

    private fun assertNull(value: Any?) {
        assertEquals(null, value)
    }
}
