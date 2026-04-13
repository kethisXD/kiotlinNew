package com.example.data.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : Table("users") {
    val id = long("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 20)
    override val primaryKey = PrimaryKey(id)
}

object Products : Table("products") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100)
    val description = text("description")
    val price = double("price")
    val stock = integer("stock")
    override val primaryKey = PrimaryKey(id)
}

object Orders : Table("orders") {
    val id = long("id").autoIncrement()
    val userId = reference("user_id", Users.id)
    val status = varchar("status", 20)
    val total = double("total_price")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object OrderItems : Table("order_items") {
    val id = long("id").autoIncrement()
    val orderId = reference("order_id", Orders.id)
    val productId = reference("product_id", Products.id)
    val quantity = integer("quantity")
    val price = double("price")
    override val primaryKey = PrimaryKey(id)
}

object AuditLogs : Table("audit_logs") {
    val id = long("id").autoIncrement()
    val action = varchar("action", 100)
    val details = text("details")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}
