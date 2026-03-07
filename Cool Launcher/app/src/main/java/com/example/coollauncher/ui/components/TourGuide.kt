package com.example.coollauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.example.coollauncher.ui.AppIcons
import androidx.compose.material3.Text
import com.example.coollauncher.ui.theme.AppShapes
import com.example.coollauncher.ui.theme.AppTypography
import com.example.coollauncher.ui.theme.LocalAccentColors
import com.example.coollauncher.ui.theme.overlayScrim
import com.example.coollauncher.ui.theme.surfaceCard
import com.example.coollauncher.ui.theme.textPrimary
import com.example.coollauncher.ui.theme.textSecondary

enum class TourIcon { Welcome, CreateGroup, FoldersApps, Settings, Done }

data class TourStep(
    val title: String,
    val description: String,
    val iconKey: TourIcon = TourIcon.Welcome,
)

@Composable
fun TourGuide(
    currentStep: Int,
    totalSteps: Int,
    steps: List<TourStep>,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (currentStep >= steps.size) return
    val step = steps[currentStep]
    val accentColors = LocalAccentColors.current
    val isLastStep = currentStep == steps.size - 1
    val isFirstStep = currentStep == 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(overlayScrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(AppShapes.dialog)
                .background(surfaceCard)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { },
                )
                .padding(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                    SecondaryIconButton(
                        onClick = onSkip,
                        icon = {
                            Icon(
                                painter = painterResource(AppIcons.Close),
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        contentDescription = "Skip",
                    )
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(accentColors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    when (step.iconKey) {
                        TourIcon.Welcome -> Icon(painter = painterResource(AppIcons.Folder), contentDescription = null, modifier = Modifier.size(32.dp), tint = accentColors.primary)
                        TourIcon.CreateGroup -> Icon(painter = painterResource(AppIcons.Add), contentDescription = null, modifier = Modifier.size(32.dp), tint = accentColors.primary)
                        TourIcon.FoldersApps -> Icon(painter = painterResource(AppIcons.TouchApp), contentDescription = null, modifier = Modifier.size(32.dp), tint = accentColors.primary)
                        TourIcon.Settings -> Icon(painter = painterResource(AppIcons.Settings), contentDescription = null, modifier = Modifier.size(32.dp), tint = accentColors.primary)
                        TourIcon.Done -> Icon(painter = painterResource(AppIcons.Check), contentDescription = null, modifier = Modifier.size(32.dp), tint = accentColors.primary)
                    }
                }
                Text(
                    text = step.title,
                    style = AppTypography.headline(),
                    color = textPrimary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = step.description,
                    style = AppTypography.body(),
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (!isFirstStep) {
                            SecondaryButton(
                                text = "Back",
                                onClick = onBack,
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        PrimaryButton(
                            text = if (isLastStep) "Done" else "Next",
                            onClick = onNext,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(totalSteps) { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (index == currentStep) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentStep) accentColors.primary
                                    else textSecondary.copy(alpha = 0.4f),
                                ),
                        )
                    }
                }
            }
        }
    }
}
