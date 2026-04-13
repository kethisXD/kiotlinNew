package com.example.data.repository

import com.example.domain.repository.TransactionManager
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class TransactionManagerImpl : TransactionManager {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return newSuspendedTransaction(Dispatchers.IO) {
            block()
        }
    }
}
