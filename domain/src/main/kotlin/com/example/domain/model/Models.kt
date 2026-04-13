package com.example.domain.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}

enum class Role {
    USER, ADMIN
}

@Serializable
data class User(
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val role: Role
)

@Serializable
data class Product(
    val id: Long = 0,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int
)

enum class OrderStatus {
    PENDING, COMPLETED, CANCELLED
}

@Serializable
data class Order(
    val id: Long = 0,
    val userId: Long,
    val status: OrderStatus,
    val totalPrice: Double,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.now()
)

@Serializable
data class OrderItem(
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val quantity: Int,
    val price: Double
)

@Serializable
data class AuditLog(
    val id: Long = 0,
    val action: String,
    val details: String,
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.now()
)

