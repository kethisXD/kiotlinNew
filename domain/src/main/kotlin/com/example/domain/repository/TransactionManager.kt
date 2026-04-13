package com.example.domain.repository

interface TransactionManager {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
