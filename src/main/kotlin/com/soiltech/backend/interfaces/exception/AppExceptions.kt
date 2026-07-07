package com.soiltech.backend.interfaces.exception

class NotFoundException(message: String) : RuntimeException(message)
class UnauthorizedException(message: String = "Unauthorized") : RuntimeException(message)
class ForbiddenException(message: String = "Forbidden") : RuntimeException(message)
class BadRequestException(message: String) : RuntimeException(message)
class ConflictException(message: String) : RuntimeException(message)
class UnprocessableEntityException(message: String) : RuntimeException(message)
