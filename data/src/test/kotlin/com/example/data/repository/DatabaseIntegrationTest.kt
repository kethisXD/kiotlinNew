package com.example.data.repository

import com.example.data.db.DatabaseFactory
import com.example.domain.model.Product
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Testcontainers
class DatabaseIntegrationTest {

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

    @Test
    fun `test product repository save and find`() = runBlocking {
        val repo = ProductRepositoryImpl()
        var createdProduct: Product? = null

        // Need to run inside transaction for Expose mapping (unless using Suspended transaction)
        val tm = TransactionManagerImpl()
        
        tm.runInTransaction {
            val product = Product(name = "Laptop", description = "Test", price = 1000.0, stock = 10)
            createdProduct = repo.create(product)
        }
        
        // Assert
        assertNotNull(createdProduct)
        assert(createdProduct!!.id > 0)
        
        tm.runInTransaction {
            val found = repo.findById(createdProduct!!.id)
            assertNotNull(found)
            assertEquals("Laptop", found!!.name)
        }
    }
}
