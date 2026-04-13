package com.example.data.repository

import com.example.data.db.Products
import com.example.domain.model.Product
import com.example.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ProductRepositoryImpl : ProductRepository {
    private fun ResultRow.toProduct(): Product = Product(
        id = this[Products.id],
        name = this[Products.name],
        description = this[Products.description],
        price = this[Products.price],
        stock = this[Products.stock]
    )

    override suspend fun create(product: Product): Product = newSuspendedTransaction(Dispatchers.IO) {
        val id = Products.insert {
            it[name] = product.name
            it[description] = product.description
            it[price] = product.price
            it[stock] = product.stock
        } get Products.id
        product.copy(id = id)
    }

    override suspend fun update(id: Long, product: Product): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        val updatedRows = Products.update({ Products.id eq id }) {
            it[name] = product.name
            it[description] = product.description
            it[price] = product.price
            it[stock] = product.stock
        }
        updatedRows > 0
    }

    override suspend fun delete(id: Long): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        val deletedRows = Products.deleteWhere { Products.id eq id }
        deletedRows > 0
    }

    override suspend fun findById(id: Long): Product? = newSuspendedTransaction(Dispatchers.IO) {
        Products.selectAll().where { Products.id eq id }
            .singleOrNull()?.toProduct()
    }

    override suspend fun findAll(): List<Product> = newSuspendedTransaction(Dispatchers.IO) {
        Products.selectAll().map { it.toProduct() }
    }

    override suspend fun decreaseStock(id: Long, amount: Int): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        val product = Products.selectAll().where { Products.id eq id }
            .singleOrNull()?.toProduct() ?: return@newSuspendedTransaction false
        if (product.stock < amount) return@newSuspendedTransaction false
        
        val updatedRows = Products.update({ Products.id eq id }) {
            it[stock] = product.stock - amount
        }
        updatedRows > 0
    }
}
