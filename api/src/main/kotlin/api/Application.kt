package api

import api.auth.JwtConfig
import api.di.appModule
import api.routes.*
import api.worker.RabbitMqWorker
import com.example.data.db.DatabaseFactory
import com.rabbitmq.client.Channel
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    // Database Init
    fun getConfig(key: String, default: String) =
        System.getProperty(key) ?: System.getenv(key) ?: default

    val dbUrl = getConfig("DB_URL", "jdbc:postgresql://localhost:5432/ktor_db")
    val dbUser = getConfig("DB_USER", "postgres")
    val dbPass = getConfig("DB_PASSWORD", "postgres")
    DatabaseFactory.init(dbUrl, dbUser, dbPass)

    install(Koin) {
        modules(appModule)
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Ktor Server"
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    val rabbitChannel by inject<Channel>()
    val worker = RabbitMqWorker(rabbitChannel)
    worker.start()

    routing {
        swaggerUI(path = "swagger-ui", swaggerFile = "openapi/documentation.yaml")
        authRouting()
        productRouting()
        orderRouting()
    }
}

