package api.di

import com.example.data.cache.RedisProductCache
import com.example.data.messaging.RabbitMqEventProducer
import com.example.data.repository.*
import com.example.domain.cache.ProductCache
import com.example.domain.messaging.EventProducer
import com.example.domain.repository.*
import com.example.domain.service.OrderService
import com.example.domain.service.ProductService
import com.example.service.OrderServiceImpl
import com.example.service.ProductServiceImpl
import com.example.service.UserService
import com.rabbitmq.client.ConnectionFactory
import io.lettuce.core.RedisClient
import org.koin.dsl.module

private fun cfg(key: String, default: String) =
    System.getProperty(key) ?: System.getenv(key) ?: default

val appModule = module {
    // Database & Transactions
    single<TransactionManager> { TransactionManagerImpl() }
    
    // Repositories
    single<UserRepository> { UserRepositoryImpl() }
    single<ProductRepository> { ProductRepositoryImpl() }
    single<OrderRepository> { OrderRepositoryImpl() }
    single<AuditLogRepository> { AuditLogRepositoryImpl() }
    
    // Infrastructure
    single { 
        val host = cfg("REDIS_HOST", "localhost")
        val port = cfg("REDIS_PORT", "6379").toIntOrNull() ?: 6379
        RedisClient.create("redis://$host:$port")
    }
    single { get<RedisClient>().connect() }
    single<ProductCache> { RedisProductCache(get()) }
    
    single {
        val factory = ConnectionFactory()
        factory.host = cfg("RABBITMQ_HOST", "localhost")
        factory.port = cfg("RABBITMQ_PORT", "5672").toIntOrNull() ?: 5672
        factory.newConnection()
    }
    single { get<com.rabbitmq.client.Connection>().createChannel() }
    single<EventProducer> { RabbitMqEventProducer(get()) }
    
    // Services
    single { UserService(get()) }
    single<ProductService> { ProductServiceImpl(get(), get()) }
    single<OrderService> { OrderServiceImpl(get(), get(), get(), get(), get()) }
}
