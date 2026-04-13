package api.routes

import api.auth.JwtConfig
import com.example.service.UserService
import io.github.smiley4.ktorswaggerui.dsl.post
import io.github.smiley4.ktorswaggerui.dsl.route
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable data class AuthRequest(val username: String, val passwordRaw: String)
@Serializable data class AuthResponse(val token: String)

fun Route.authRouting() {
    val userService by inject<UserService>()

    route("/auth", {
        tags = listOf("Auth")
    }) {
        post("/register", {
            description = "Register a new user"
            request { body<AuthRequest>() }
            response {
                HttpStatusCode.Created to { description = "Created user token"; body<AuthResponse>() }
                HttpStatusCode.BadRequest to { description = "Validation failed" }
            }
        }) {
            val req = call.receive<AuthRequest>()
            try {
                val user = userService.register(req.username, req.passwordRaw)
                val token = JwtConfig.generateToken(user.username, user.id, user.role.name)
                call.respond(HttpStatusCode.Created, AuthResponse(token))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Unknown error")))
            }
        }

        post("/login", {
            description = "Login and get token"
            request { body<AuthRequest>() }
            response {
                HttpStatusCode.OK to { description = "Successful login"; body<AuthResponse>() }
                HttpStatusCode.Unauthorized to { description = "Invalid credentials" }
            }
        }) {
            val req = call.receive<AuthRequest>()
            try {
                val user = userService.authenticate(req.username, req.passwordRaw)
                val token = JwtConfig.generateToken(user.username, user.id, user.role.name)
                call.respond(HttpStatusCode.OK, AuthResponse(token))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to (e.message ?: "Unauthorized")))
            }
        }
    }
}
