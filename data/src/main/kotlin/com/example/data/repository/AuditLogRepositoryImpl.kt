package com.example.data.repository

import com.example.data.db.AuditLogs
import com.example.domain.model.AuditLog
import com.example.domain.repository.AuditLogRepository
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class AuditLogRepositoryImpl : AuditLogRepository {
    override suspend fun create(log: AuditLog): AuditLog = newSuspendedTransaction(Dispatchers.IO) {
        val id = AuditLogs.insert {
            it[action] = log.action
            it[details] = log.details
            it[createdAt] = log.createdAt
        } get AuditLogs.id
        log.copy(id = id)
    }
}

