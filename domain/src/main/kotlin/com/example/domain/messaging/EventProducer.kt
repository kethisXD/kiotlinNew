package com.example.domain.messaging

interface EventProducer {
    suspend fun publishOrderCreated(orderId: Long, userId: Long)
}
