package com.example.data.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init(url: String, user: String, pass: String) {
        val config = HikariConfig().apply {
            jdbcUrl = url
            username = user
            password = pass
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        val dataSource = HikariDataSource(config)
        
        // Run Flyway Migrations
        Flyway.configure()
            .dataSource(dataSource)
            .load()
            .migrate()
            
        Database.connect(dataSource)
    }
}
