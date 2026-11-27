package com.elderassist.app.core.result

enum class ErrorCategory {
    PERMISSION_MISSING,
    ACCESSIBILITY_DISABLED,
    UNSUPPORTED_ON_DEVICE,
    AMBIGUOUS_INPUT,
    VALIDATION_REJECTED,
    STALE_SCREEN_STATE,
    NETWORK_OR_AI_ERROR,
    CANCELLED,
    NOT_FOUND,
    UNKNOWN
}

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>

    data class Failure(
        val category: ErrorCategory,
        val userMessage: String,
        val technicalDetail: String? = null,
        val recoverable: Boolean = true
    ) : AppResult<Nothing>
}
