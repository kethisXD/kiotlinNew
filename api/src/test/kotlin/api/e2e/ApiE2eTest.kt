package api.e2e

import api.module
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class ApiE2eTest {

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @Container
        val redisContainer = GenericContainer<Nothing>("redis:alpine").apply {
            withExposedPorts(6379)
        }

        @Container
        val rabbitContainer = RabbitMQContainer("rabbitmq:3-management-alpine").apply {
            withExposedPorts(5672, 15672)
        }

        @JvmStatic
        @BeforeAll
        fun setUpAll() {
            postgresContainer.start()
            redisContainer.start()
            rabbitContainer.start()

            // Set env vars so Application.module() can connect to testcontainers
            System.setProperty("DB_URL", postgresContainer.jdbcUrl)
            System.setProperty("DB_USER", postgresContainer.username)
            System.setProperty("DB_PASSWORD", postgresContainer.password)
            System.setProperty("REDIS_HOST", redisContainer.host)
            System.setProperty("REDIS_PORT", redisContainer.getMappedPort(6379).toString())
            System.setProperty("RABBITMQ_HOST", rabbitContainer.host)
            System.setProperty("RABBITMQ_PORT", rabbitContainer.getMappedPort(5672).toString())
        }
    }

    @Test
    fun `test register user endpoint`() = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "ktor.deployment.port" to "8080",
                "ktor.application.modules.0" to "api.ApplicationKt.module"
            )
        }
        application {
            module()
        }

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"username": "e2euser1", "passwordRaw": "testpass"}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assert(response.bodyAsText().contains("token"))
    }

    @Test
    fun `test register and login workflow`() = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "ktor.deployment.port" to "8080",
                "ktor.application.modules.0" to "api.ApplicationKt.module"
            )
        }
        application {
            module()
        }

        // Register
        client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"username": "e2elogin", "passwordRaw": "mypass"}""")
        }

        // Login
        val loginResp = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username": "e2elogin", "passwordRaw": "mypass"}""")
        }

        assertEquals(HttpStatusCode.OK, loginResp.status)
        assert(loginResp.bodyAsText().contains("token"))
    }

    @Test
    fun `test get products returns empty list`() = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "ktor.deployment.port" to "8080",
                "ktor.application.modules.0" to "api.ApplicationKt.module"
            )
        }
        application {
            module()
        }

        val response = client.get("/products")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("[]", response.bodyAsText().trim())
    }

    @Test
    fun `test unauthorized access to orders`() = testApplication {
        environment {
            config = io.ktor.server.config.MapApplicationConfig(
                "ktor.deployment.port" to "8080",
                "ktor.application.modules.0" to "api.ApplicationKt.module"
            )
        }
        application {
            module()
        }

        val response = client.get("/orders")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
