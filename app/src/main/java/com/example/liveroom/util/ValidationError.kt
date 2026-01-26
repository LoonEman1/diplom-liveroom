package com.example.liveroom.util

import com.example.liveroom.R

sealed class ValidationError(val errorResId: Int) {

    object UsernameRequired : ValidationError(R.string.error_username_required)
    object UsernameTooShort : ValidationError(R.string.error_username_too_short)
    object UsernameTooLong : ValidationError(R.string.error_username_too_long)
    object UsernameInvalidFormat : ValidationError(R.string.error_username_invalid_format)

    object EmailRequired : ValidationError(R.string.error_email_required)
    object EmailTooLong : ValidationError(R.string.error_email_too_long)
    object EmailInvalid : ValidationError(R.string.error_email_invalid)

    object PasswordRequired : ValidationError(R.string.error_password_required)
    object PasswordTooShort : ValidationError(R.string.error_password_too_short)
    object PasswordTooLong : ValidationError(R.string.error_password_too_long)
    object PasswordInvalid : ValidationError(R.string.error_password_invalid)

    object ConfirmPasswordRequired : ValidationError(R.string.error_confirm_password_required)
    object PasswordsDoNotMatch : ValidationError(R.string.error_passwords_do_not_match)
}