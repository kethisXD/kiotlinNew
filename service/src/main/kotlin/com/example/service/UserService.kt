package com.example.service

import com.example.domain.exception.AuthenticationException
import com.example.domain.exception.ValidationException
import com.example.domain.model.Role
import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt

class UserService(
    private val userRepository: UserRepository
) {
    suspend fun register(username: String, passwordRaw: String, role: Role = Role.USER): User {
        if (username.isBlank() || passwordRaw.isBlank()) {
            throw ValidationException("Username and password cannot be empty")
        }
        val existing = userRepository.findByUsername(username)
        if (existing != null) {
            throw ValidationException("User with this username already exists")
        }
        
        val passwordHash = BCrypt.hashpw(passwordRaw, BCrypt.gensalt())
        val user = User(username = username, passwordHash = passwordHash, role = role)
        return userRepository.create(user)
    }

    suspend fun authenticate(username: String, passwordRaw: String): User {
        val user = userRepository.findByUsername(username)
            ?: throw AuthenticationException("Invalid username or password")
            
        if (!BCrypt.checkpw(passwordRaw, user.passwordHash)) {
            throw AuthenticationException("Invalid username or password")
        }
        return user
    }
}
