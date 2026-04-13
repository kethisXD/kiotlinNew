package com.example.data.messaging

import com.example.domain.messaging.EventProducer
import com.rabbitmq.client.Channel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class OrderEventMsg(val orderId: Long, val userId: Long)

class RabbitMqEventProducer(
    private val channel: Channel,
    private val queueName: String = "orders_queue"
) : EventProducer {

    init {
        channel.queueDeclare(queueName, true, false, false, null)
    }

    override suspend fun publishOrderCreated(orderId: Long, userId: Long) {
        val msg = OrderEventMsg(orderId, userId)
        val json = Json.encodeToString(msg)
        channel.basicPublish("", queueName, null, json.toByteArray())
    }
}
