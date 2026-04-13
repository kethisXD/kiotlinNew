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
        val host = System.getenv("REDIS_HOST") ?: "localhost"
        val port = System.getenv("REDIS_PORT")?.toIntOrNull() ?: 6379
        RedisClient.create("redis://$host:$port")
    }
    single { get<RedisClient>().connect() }
    single<ProductCache> { RedisProductCache(get()) }
    
    single {
        val factory = ConnectionFactory()
        factory.host = System.getenv("RABBITMQ_HOST") ?: "localhost"
        factory.port = System.getenv("RABBITMQ_PORT")?.toIntOrNull() ?: 5672
        factory.newConnection()
    }
    single { get<com.rabbitmq.client.Connection>().createChannel() }
    single<EventProducer> { RabbitMqEventProducer(get()) }
    
    // Services
    single { UserService(get()) }
    single<ProductService> { ProductServiceImpl(get(), get()) }
    single<OrderService> { OrderServiceImpl(get(), get(), get(), get(), get()) }
}
