package com.instashow.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.instashow.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session")

private val accountIdKey = stringPreferencesKey("account_id")
private val emailKey = stringPreferencesKey("email")
private val displayNameKey = stringPreferencesKey("display_name")

class AuthRepository(context: Context) {
    private val appContext = context.applicationContext
    private val credentialManager = CredentialManager.create(appContext)
    private val dataStore = appContext.sessionDataStore

    val session: Flow<UserSession?> = dataStore.data.map { prefs ->
        val accountId = prefs[accountIdKey] ?: return@map null
        UserSession(
            accountId = accountId,
            email = prefs[emailKey].orEmpty(),
            displayName = prefs[displayNameKey].orEmpty(),
        )
    }

    suspend fun signIn(activityContext: Context): SignInOutcome {
        val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (clientId.isBlank()) return SignInOutcome.Error(AuthError.MissingClientId)

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
            .build()

        return try {
            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return SignInOutcome.Error(AuthError.Failed)
            }
            val google = GoogleIdTokenCredential.createFrom(credential.data)
            if (google.id.isBlank()) return SignInOutcome.Error(AuthError.Failed)
            val session = UserSession(
                accountId = google.id,
                email = google.id,
                displayName = google.displayName?.takeIf { it.isNotBlank() } ?: google.id,
            )
            save(session)
            SignInOutcome.Success(session)
        } catch (_: GetCredentialCancellationException) {
            SignInOutcome.Cancelled
        } catch (_: NoCredentialException) {
            SignInOutcome.Error(AuthError.NoAccount)
        } catch (_: GoogleIdTokenParsingException) {
            SignInOutcome.Error(AuthError.Failed)
        } catch (_: GetCredentialException) {
            SignInOutcome.Error(AuthError.Failed)
        }
    }

    suspend fun signOut() {
        runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
        dataStore.edit { it.clear() }
    }

    private suspend fun save(session: UserSession) {
        dataStore.edit { prefs ->
            prefs[accountIdKey] = session.accountId
            prefs[emailKey] = session.email
            prefs[displayNameKey] = session.displayName
        }
    }
}
