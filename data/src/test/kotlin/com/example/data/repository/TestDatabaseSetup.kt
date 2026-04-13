package com.example.data.repository

import com.example.data.db.DatabaseFactory
import org.testcontainers.containers.PostgreSQLContainer

object TestDatabaseSetup {
    val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
    }

    private var initialized = false

    fun init() {
        if (!initialized) {
            postgresContainer.start()
            DatabaseFactory.init(
                postgresContainer.jdbcUrl,
                postgresContainer.username,
                postgresContainer.password
            )
            initialized = true
        }
    }
}
