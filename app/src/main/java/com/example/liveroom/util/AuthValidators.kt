package com.example.liveroom.util

object AuthValidators {

    private val USERNAME_PATTERN = Regex("^[a-zA-Z0-9._-]+$")
    private val EMAIL_PATTERN = Regex(
        "^[A-Za-z0-9._%+-]{1,64}@[A-Za-z0-9-]{1,63}(\\.[A-Za-z0-9-]{1,63})*(\\.(com|ru))$"
    )
    private val PASSWORD_PATTERN = Regex(
        "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{12,72}$"
    )

    fun validateUsername(username: String): ValidationError? {
        return when {
            username.isBlank() -> ValidationError.UsernameRequired
            username.length < 3 -> ValidationError.UsernameTooShort
            username.length > 32 -> ValidationError.UsernameTooLong
            !USERNAME_PATTERN.matches(username) ->
                ValidationError.UsernameInvalidFormat
            else -> null
        }
    }

    fun validateEmail(email: String): ValidationError? {
        return when {
            email.isBlank() -> ValidationError.EmailRequired
            email.length > 254 -> ValidationError.EmailTooLong
            !EMAIL_PATTERN.matches(email) -> ValidationError.EmailInvalid
            else -> null
        }
    }

    fun validatePassword(password: String): ValidationError? {
        return when {
            password.isBlank() -> ValidationError.PasswordRequired
            password.length < 12 -> ValidationError.PasswordTooShort
            password.length > 72 -> ValidationError.PasswordTooLong
            !PASSWORD_PATTERN.matches(password) ->
                ValidationError.PasswordInvalid
            else -> null
        }
    }

    fun validateConfirmPassword(
        password: String,
        confirmPassword: String
    ): ValidationError? {
        return when {
            confirmPassword.isBlank() -> ValidationError.ConfirmPasswordRequired
            password != confirmPassword -> ValidationError.PasswordsDoNotMatch
            else -> null
        }
    }

    fun validateRegistration(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        validateUsername(username)?.let { errors.add(it) }
        validateEmail(email)?.let { errors.add(it) }
        validatePassword(password)?.let { errors.add(it) }
        validateConfirmPassword(password, confirmPassword)?.let { errors.add(it) }

        return errors
    }

    fun validateLogin(username: String, password: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        validateUsername(username)?.let { errors.add(it) }
        if (password.isBlank()) {
            errors.add(ValidationError.PasswordRequired)
        }

        return errors
    }
}

