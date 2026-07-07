package com.soiltech.backend.interfaces.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    @JsonProperty("status_code")
    val statusCode: Int? = null,
    val meta: PaginationMeta? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null, statusCode: Int = 200): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message, statusCode = statusCode)

        fun <T> success(
            data: T,
            message: String? = null,
            statusCode: Int = 200,
            meta: PaginationMeta
        ): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message, statusCode = statusCode, meta = meta)

        fun <T> created(data: T, message: String? = "Created successfully"): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message, statusCode = 201)

        fun error(message: String, statusCode: Int): ApiResponse<Unit?> =
            ApiResponse(success = false, message = message, statusCode = statusCode)
    }
}
