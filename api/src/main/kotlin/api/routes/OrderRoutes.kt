package api.routes

import com.example.domain.model.Order
import com.example.domain.service.OrderRequest
import com.example.domain.service.OrderService
import io.github.smiley4.ktorswaggerui.dsl.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.orderRouting() {
    val orderService by inject<OrderService>()

    authenticate("auth-jwt") {
        route("/orders", { 
            tags = listOf("Orders")
            securitySchemeName = "auth-jwt"
        }) {
            post({
                description = "Create an order"
                request { body<List<OrderRequest>>() }
                response { 
                    HttpStatusCode.Created to { body<Order>() }
                    HttpStatusCode.BadRequest to { description = "Invalid stock or product" }
                }
            }) {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong() ?: return@post call.respond(HttpStatusCode.Unauthorized)
                
                val req = call.receive<List<OrderRequest>>()
                try {
                    val order = orderService.createOrder(userId, req)
                    call.respond(HttpStatusCode.Created, order)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            get({
                description = "Get user's history of orders"
                response { HttpStatusCode.OK to { body<List<Order>>() } }
            }) {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong() ?: return@get call.respond(HttpStatusCode.Unauthorized)
                val orders = orderService.getUserOrders(userId)
                call.respond(HttpStatusCode.OK, orders)
            }

            delete("/{id}", {
                description = "Cancel an order"
                request { pathParameter<Long>("id") }
                response { HttpStatusCode.NoContent to {} }
            }) {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong() ?: return@delete call.respond(HttpStatusCode.Unauthorized)
                val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                
                try {
                    orderService.cancelOrder(id, userId)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                }
            }
        }
        
        get("/stats/orders", {
            tags = listOf("Admin Stats")
            securitySchemeName = "auth-jwt"
            description = "Get order stats (Admin)"
            response { HttpStatusCode.OK to { body<Map<String, String>>() } }
        }) {
            val principal = call.principal<JWTPrincipal>()
            if (principal?.payload?.getClaim("role")?.asString() != "ADMIN") return@get call.respond(HttpStatusCode.Forbidden)
            
            call.respond(orderService.getStats())
        }
    }
}
