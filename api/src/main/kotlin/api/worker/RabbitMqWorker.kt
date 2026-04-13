package api.worker

import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class RabbitMqWorker(private val channel: Channel) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val scope = CoroutineScope(Dispatchers.IO)

    fun start() {
        val queueName = "orders_queue"
        channel.queueDeclare(queueName, true, false, false, null)
        
        val deliverCallback = DeliverCallback { _, delivery ->
            scope.launch {
                val message = String(delivery.body, Charsets.UTF_8)
                logger.info(" [x] Received order event: '$message'")
                logger.info(" [x] Mock sending email to user...")
            }
        }
        
        channel.basicConsume(queueName, true, deliverCallback, { _ -> })
        logger.info("Worker started, waiting for messages on '$queueName'")
    }
}
