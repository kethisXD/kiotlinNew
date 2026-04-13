package com.example.data.cache

import com.example.domain.cache.ProductCache
import com.example.domain.model.Product
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int
)

fun Product.toDto() = ProductDto(id, name, description, price, stock)
fun ProductDto.toDomain() = Product(id, name, description, price, stock)

class RedisProductCache(
    redisConnection: StatefulRedisConnection<String, String>
) : ProductCache {
    
    private val commands: RedisAsyncCommands<String, String> = redisConnection.async()
    private val prefix = "product:"

    override suspend fun getProduct(id: Long): Product? {
        val json = commands.get("$prefix$id").await() ?: return null
        return try {
            Json.decodeFromString<ProductDto>(json).toDomain()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun setProduct(product: Product, ttlSeconds: Long) {
        val json = Json.encodeToString(product.toDto())
        commands.setex("$prefix${product.id}", ttlSeconds, json).await()
    }

    override suspend fun invalidate(id: Long) {
        commands.del("$prefix$id").await()
    }
}
