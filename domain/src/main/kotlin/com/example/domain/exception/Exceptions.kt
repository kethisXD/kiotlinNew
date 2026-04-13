package com.example.domain.exception

class EntityNotFoundException(message: String) : RuntimeException(message)
class InsufficientStockException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message)
class AuthenticationException(message: String) : RuntimeException(message)
