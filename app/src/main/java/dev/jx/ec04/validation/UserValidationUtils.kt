package dev.jx.ec04.validation

import android.content.Context
import android.util.Patterns
import androidx.core.text.isDigitsOnly
import dev.jx.ec04.R
import java.util.regex.Pattern

object UserValidationUtils {

    fun validateFirstname(ctx: Context, firstname: String?): ValidationResult {
        val minChars = ctx.resources.getInteger(R.integer.firstname_min_chars)
        val maxChars = ctx.resources.getInteger(R.integer.firstname_max_chars)

        return when {
            firstname.isNullOrBlank() -> {
                ValidationResult(false, ctx.getString(R.string.error_required_field))
            }
            firstname.length < minChars -> {
                ValidationResult(
                    false,
                    ctx.getString(R.string.error_min_chars, minChars)
                )
            }
            firstname.length > maxChars -> {
                ValidationResult(
                    false,
                    ctx.getString(R.string.error_max_chars, maxChars)
                )
            }
            else -> ValidationResult(true)
        }
    }

    fun validateLastname(ctx: Context, lastname: String?): ValidationResult {
        val minChars = ctx.resources.getInteger(R.integer.lastname_min_chars)
        val maxChars = ctx.resources.getInteger(R.integer.lastname_max_chars)

        return when {
            lastname.isNullOrBlank() -> {
                ValidationResult(false, ctx.getString(R.string.error_required_field))
            }
            lastname.length < minChars -> {
                ValidationResult(
                    false,
                    ctx.getString(R.string.error_min_chars, minChars)
                )
            }
            lastname.length > maxChars -> {
                ValidationResult(
                    false,
                    ctx.getString(R.string.error_max_chars, maxChars)
                )
            }
            else -> ValidationResult(true)
        }
    }

    fun validateEmail(ctx: Context, email: String?): ValidationResult {
        val emailMatcher = Patterns.EMAIL_ADDRESS.matcher(email)
        return when {
            email.isNullOrBlank() -> {
                return ValidationResult(false, ctx.getString(R.string.error_required_field))
            }
            !emailMatcher.matches() -> {
                return ValidationResult(false, ctx.getString(R.string.error_invalid_email))
            }
            else -> ValidationResult(true)
        }
    }

    fun validatePassword(ctx: Context, password: String?): ValidationResult {
        val minChars = ctx.resources.getInteger(R.integer.password_min_chars)
        val maxChars = ctx.resources.getInteger(R.integer.password_max_chars)
        val passwordMatcher =
            Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).{$minChars,$maxChars}$").matcher(password)

        return when {
            password.isNullOrEmpty() -> {
                ValidationResult(false, ctx.getString(R.string.error_required_field))
            }
            !passwordMatcher.matches() -> {
                ValidationResult(
                    false,
                    ctx.getString(R.string.error_invalid_password, minChars, maxChars)
                )
            }
            else -> ValidationResult(true)
        }
    }

    fun validateConfirmPassword(ctx: Context, expect: String?, actual: String?): ValidationResult {
        return when (expect) {
            actual -> {
                ValidationResult(true)
            }
            else -> ValidationResult(false, ctx.getString(R.string.error_invalid_confirm_password))
        }
    }

    fun validatePhoneNumber(ctx: Context, phoneNumber: String?): ValidationResult {
        val length = ctx.resources.getInteger(R.integer.phone_number_length)
        return when {
            phoneNumber.isNullOrBlank() || (phoneNumber.isDigitsOnly() && phoneNumber.length == length) -> {
                ValidationResult(true)
            }
            else -> ValidationResult(
                false,
                ctx.getString(R.string.error_invalid_phone_number, length)
            )
        }
    }
}