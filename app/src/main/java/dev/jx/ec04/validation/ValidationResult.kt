package dev.jx.ec04.validation

data class ValidationResult(
    val ok: Boolean,
    val reason: String? = null
)
