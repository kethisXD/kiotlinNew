package api.routes

import com.example.domain.model.Product
import com.example.domain.service.ProductService
import io.github.smiley4.ktorswaggerui.dsl.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable data class ProductDto(val name: String, val description: String, val price: Double, val stock: Int)

fun Route.productRouting() {
    val productService by inject<ProductService>()

    route("/products", { tags = listOf("Products") }) {
        get({
            description = "Get all products"
            response { HttpStatusCode.OK to { body<List<Product>>() } }
        }) {
            call.respond(productService.getProducts())
        }

        get("/{id}", {
            description = "Get a specific product"
            request { pathParameter<Long>("id") }
            response {
                HttpStatusCode.OK to { body<Product>() }
                HttpStatusCode.NotFound to { description = "Product not found" }
            }
        }) {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            try {
                call.respond(productService.getProduct(id))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
            }
        }
        
        authenticate("auth-jwt") {
            post({
                description = "Add a new product (Admin)"
                securitySchemeName = "auth-jwt"
                request { body<ProductDto>() }
                response { HttpStatusCode.Created to { body<Product>() } }
            }) {
                val principal = call.principal<JWTPrincipal>()
                val role = principal?.payload?.getClaim("role")?.asString()
                if (role != "ADMIN") return@post call.respond(HttpStatusCode.Forbidden)
                
                val req = call.receive<ProductDto>()
                val created = productService.addProduct(req.name, req.description, req.price, req.stock)
                call.respond(HttpStatusCode.Created, created)
            }

            put("/{id}", {
                description = "Update product (Admin)"
                securitySchemeName = "auth-jwt"
                request { 
                    pathParameter<Long>("id")
                    body<ProductDto>()
                }
                response { HttpStatusCode.OK to { body<Product>() } }
            }) {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.payload?.getClaim("role")?.asString() != "ADMIN") return@put call.respond(HttpStatusCode.Forbidden)

                val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
                val req = call.receive<ProductDto>()
                try {
                    val updated = productService.updateProduct(id, req.name, req.description, req.price, req.stock)
                    call.respond(HttpStatusCode.OK, updated)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                }
            }

            delete("/{id}", {
                description = "Delete product (Admin)"
                securitySchemeName = "auth-jwt"
                request { pathParameter<Long>("id") }
                response { HttpStatusCode.NoContent to {} }
            }) {
                val principal = call.principal<JWTPrincipal>()
                if (principal?.payload?.getClaim("role")?.asString() != "ADMIN") return@delete call.respond(HttpStatusCode.Forbidden)

                val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
                try {
                    productService.deleteProduct(id)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to e.message))
                }
            }
        }
    }
}
