package com.instashow.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instashow.R
import com.instashow.auth.AuthError
import com.instashow.auth.AuthViewModel
import com.instashow.ui.theme.Ink
import com.instashow.ui.theme.Lime
import com.instashow.ui.theme.PrimaryButton
import com.instashow.ui.theme.SecondaryButton
import com.instashow.ui.theme.TextPrimary
import com.instashow.ui.theme.TextSecondary

/** First launch. Sign-in is optional: nothing in the app needs an account yet. */
@Composable
fun WelcomeScreen(viewModel: AuthViewModel, onGetStarted: () -> Unit) {
    val signingIn by viewModel.signingIn.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = LocalAppSnackbar.current
    val missingClient = stringResource(R.string.sign_in_missing_client)
    val noAccount = stringResource(R.string.sign_in_no_account)
    val signInFailed = stringResource(R.string.sign_in_failed)
    LaunchedEffect(error) {
        val current = error ?: return@LaunchedEffect
        snackbar.showSnackbar(
            when (current) {
                AuthError.MissingClientId -> missingClient
                AuthError.NoAccount -> noAccount
                AuthError.Failed -> signInFailed
            },
        )
        viewModel.clearError()
    }

    Scaffold(snackbarHost = { AppSnackbarHost() }, containerColor = Ink) { _ ->
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.home_backdrop),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Ink.copy(alpha = 0.35f),
                            0.45f to Ink.copy(alpha = 0.55f),
                            0.72f to Ink.copy(alpha = 0.95f),
                            1f to Ink,
                        ),
                    ),
            )
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 3),
                color = TextPrimary,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(24.dp),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            ) {
                Text(
                    text = stringResource(R.string.welcome_headline),
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.welcome_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(32.dp))
                PrimaryButton(onClick = onGetStarted) {
                    Text(stringResource(R.string.get_started), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(12.dp))
                SecondaryButton(onClick = { viewModel.signIn(context) }, enabled = !signingIn) {
                    if (signingIn) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Lime, trackColor = Color.Transparent)
                    } else {
                        Icon(AppIcons.Google, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.sign_in_button), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
