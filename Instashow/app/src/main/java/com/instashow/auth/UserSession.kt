package com.instashow.auth

data class UserSession(
    val accountId: String,
    val email: String,
    val displayName: String,
)

enum class AuthError {
    MissingClientId,
    NoAccount,
    Failed,
}

sealed interface SignInOutcome {
    data class Success(val session: UserSession) : SignInOutcome

    data object Cancelled : SignInOutcome

    data class Error(val kind: AuthError) : SignInOutcome
}
