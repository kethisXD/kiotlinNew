package com.example.service

import com.example.domain.exception.AuthenticationException
import com.example.domain.exception.ValidationException
import com.example.domain.model.Role
import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.mindrot.jbcrypt.BCrypt
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val userService = UserService(userRepository)

    @Test
    fun `register should throw exception if username is blank`() = runBlocking {
        assertThrows<ValidationException> {
            userService.register("", "pass")
        }
    }

    @Test
    fun `register should throw exception if user already exists`() = runBlocking {
        coEvery { userRepository.findByUsername("test") } returns User(1, "test", "hash", Role.USER)
        
        assertThrows<ValidationException> {
            userService.register("test", "pass")
        }
    }

    @Test
    fun `authenticate should return user on success`() = runBlocking {
        val hash = BCrypt.hashpw("realpass", BCrypt.gensalt())
        val mockUser = User(1, "test", hash, Role.USER)
        coEvery { userRepository.findByUsername("test") } returns mockUser
        
        val user = userService.authenticate("test", "realpass")
        assertEquals(1, user.id)
    }

    @Test
    fun `authenticate should throw exception on wrong password`() = runBlocking {
        val hash = BCrypt.hashpw("realpass", BCrypt.gensalt())
        val mockUser = User(1, "test", hash, Role.USER)
        coEvery { userRepository.findByUsername("test") } returns mockUser
        
        assertThrows<AuthenticationException> {
            userService.authenticate("test", "wrongpass")
        }
    }
}
