package com.example.data.repository

import com.example.data.db.Users
import com.example.domain.model.Role
import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepositoryImpl : UserRepository {
    private fun ResultRow.toUser(): User = User(
        id = this[Users.id],
        username = this[Users.username],
        passwordHash = this[Users.passwordHash],
        role = Role.valueOf(this[Users.role])
    )

    override suspend fun create(user: User): User = newSuspendedTransaction(Dispatchers.IO) {
        val id = Users.insert {
            it[username] = user.username
            it[passwordHash] = user.passwordHash
            it[role] = user.role.name
        } get Users.id
        user.copy(id = id)
    }

    override suspend fun findByUsername(username: String): User? = newSuspendedTransaction(Dispatchers.IO) {
        Users.selectAll().where { Users.username eq username }
            .singleOrNull()?.toUser()
    }

    override suspend fun findById(id: Long): User? = newSuspendedTransaction(Dispatchers.IO) {
        Users.selectAll().where { Users.id eq id }
            .singleOrNull()?.toUser()
    }
}

