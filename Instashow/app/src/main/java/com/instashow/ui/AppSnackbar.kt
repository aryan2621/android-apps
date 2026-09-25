package com.instashow.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import com.instashow.ui.theme.SurfaceHigh
import com.instashow.ui.theme.TextPrimary

val LocalAppSnackbar = staticCompositionLocalOf<SnackbarHostState> {
    error("Snackbar host is missing")
}

@Composable
fun AppSnackbarHost() {
    SnackbarHost(hostState = LocalAppSnackbar.current) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = SurfaceHigh,
            contentColor = TextPrimary,
            shape = RoundedCornerShape(14.dp),
        )
    }
}
