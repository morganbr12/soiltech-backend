package com.soiltech.backend.interfaces.advice

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.soiltech.backend.interfaces.exception.*
import com.soiltech.backend.interfaces.response.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ApiResponse<Unit?>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.message ?: "Not found", 404))

    @ExceptionHandler(UnauthorizedException::class, AuthenticationException::class, BadCredentialsException::class)
    fun handleUnauthorized(ex: RuntimeException): ResponseEntity<ApiResponse<Unit?>> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ex.message ?: "Unauthorized", 401))

    @ExceptionHandler(ForbiddenException::class, AccessDeniedException::class)
    fun handleForbidden(ex: RuntimeException): ResponseEntity<ApiResponse<Unit?>> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ex.message ?: "Forbidden", 403))

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException): ResponseEntity<ApiResponse<Unit?>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.message ?: "Bad request", 400))

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException): ResponseEntity<ApiResponse<Unit?>> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.message ?: "Conflict", 409))

    @ExceptionHandler(UnprocessableEntityException::class)
    fun handleUnprocessable(ex: UnprocessableEntityException): ResponseEntity<ApiResponse<Unit?>> =
        ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiResponse.error(ex.message ?: "Unprocessable entity", 422))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit?>> {
        val errors = ex.bindingResult.allErrors.joinToString("; ") { error ->
            if (error is FieldError) "${error.field}: ${error.defaultMessage}"
            else error.defaultMessage ?: "Validation error"
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(errors, 400))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ApiResponse<Unit?>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Invalid value '${ex.value}' for parameter '${ex.name}'", 400))

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Unit?>> {
        val cause = ex.cause
        val message = if (cause is InvalidFormatException && cause.targetType.isEnum) {
            val field = cause.path.lastOrNull()?.fieldName ?: "field"
            val valid = cause.targetType.enumConstants.joinToString(", ") {
                (it as Enum<*>).name.lowercase()
            }
            "Invalid value '${cause.value}' for '$field'. Accepted values: $valid"
        } else {
            "Malformed request body"
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message, 400))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ApiResponse<Unit?>> {
        log.error("Unhandled exception", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An unexpected error occurred", 500))
    }
}
