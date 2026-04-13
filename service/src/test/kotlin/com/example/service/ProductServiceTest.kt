package com.example.service

import com.example.domain.cache.ProductCache
import com.example.domain.exception.EntityNotFoundException
import com.example.domain.model.Product
import com.example.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductServiceTest {

    private val productRepository = mockk<ProductRepository>()
    private val productCache = mockk<ProductCache>(relaxed = true)
    private val productService = ProductServiceImpl(productRepository, productCache)

    @Test
    fun `getProducts should return all products from repository`() = runBlocking {
        val products = listOf(
            Product(1, "Laptop", "Gaming laptop", 1500.0, 10),
            Product(2, "Mouse", "Wireless mouse", 50.0, 100)
        )
        coEvery { productRepository.findAll() } returns products

        val result = productService.getProducts()

        assertEquals(2, result.size)
        assertEquals("Laptop", result[0].name)
    }

    @Test
    fun `getProduct should return cached product if available`() = runBlocking {
        val cached = Product(1, "Cached", "From cache", 100.0, 5)
        coEvery { productCache.getProduct(1) } returns cached

        val result = productService.getProduct(1)

        assertEquals("Cached", result.name)
        coVerify(exactly = 0) { productRepository.findById(any()) }
    }

    @Test
    fun `getProduct should fetch from DB and cache if not in cache`() = runBlocking {
        val product = Product(1, "FromDB", "From database", 200.0, 10)
        coEvery { productCache.getProduct(1) } returns null
        coEvery { productRepository.findById(1) } returns product
        coEvery { productCache.setProduct(product) } returns Unit

        val result = productService.getProduct(1)

        assertEquals("FromDB", result.name)
        coVerify(exactly = 1) { productCache.setProduct(product) }
    }

    @Test
    fun `getProduct should throw EntityNotFoundException if not found`() = runBlocking {
        coEvery { productCache.getProduct(99) } returns null
        coEvery { productRepository.findById(99) } returns null

        assertThrows<EntityNotFoundException> {
            productService.getProduct(99)
        }
    }

    @Test
    fun `addProduct should create and return product`() = runBlocking {
        val created = Product(1, "New", "Description", 100.0, 50)
        coEvery { productRepository.create(any()) } returns created

        val result = productService.addProduct("New", "Description", 100.0, 50)

        assertEquals(1, result.id)
        assertEquals("New", result.name)
    }

    @Test
    fun `updateProduct should invalidate cache after update`() = runBlocking {
        coEvery { productRepository.update(1, any()) } returns true
        coEvery { productCache.invalidate(1) } returns Unit

        val result = productService.updateProduct(1, "Updated", "Desc", 150.0, 20)

        assertEquals("Updated", result.name)
        coVerify(exactly = 1) { productCache.invalidate(1) }
    }

    @Test
    fun `updateProduct should throw if product not found`() = runBlocking {
        coEvery { productRepository.update(99, any()) } returns false

        assertThrows<EntityNotFoundException> {
            productService.updateProduct(99, "X", "X", 1.0, 1)
        }
    }

    @Test
    fun `deleteProduct should invalidate cache after delete`() = runBlocking {
        coEvery { productRepository.delete(1) } returns true
        coEvery { productCache.invalidate(1) } returns Unit

        productService.deleteProduct(1)

        coVerify(exactly = 1) { productCache.invalidate(1) }
    }

    @Test
    fun `deleteProduct should throw if product not found`() = runBlocking {
        coEvery { productRepository.delete(99) } returns false

        assertThrows<EntityNotFoundException> {
            productService.deleteProduct(99)
        }
    }
}
