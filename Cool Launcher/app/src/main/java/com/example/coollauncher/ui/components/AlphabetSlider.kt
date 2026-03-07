package com.example.coollauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.coollauncher.ui.theme.AppTypography
import com.example.coollauncher.ui.theme.LocalAccentColors
import com.example.coollauncher.ui.theme.textSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val LETTERS = ('A'..'Z').toList()

@Composable
fun AlphabetSlider(
    letterToIndex: Map<Char, Int>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    if (letterToIndex.isEmpty()) return
    val scope = rememberCoroutineScope()
    val accentColors = LocalAccentColors.current
    var sliderSize by remember { mutableStateOf(IntSize.Zero) }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    val scale by animateFloatAsState(
        targetValue = if (selectedLetter != null) 1.6f else 1f,
        label = "letterScale",
    )
    Box(
        modifier = modifier
            .fillMaxHeight()
            .onSizeChanged { sliderSize = it }
            .pointerInput(letterToIndex, listState) {
                if (sliderSize.height <= 0) return@pointerInput
                var currentY = 0f
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        currentY = offset.y
                        val letter = yToLetter(offset.y.toInt(), sliderSize.height)
                        if (letter != null && letterToIndex.containsKey(letter)) {
                            selectedLetter = letter
                            letterToIndex[letter]?.let { index ->
                                scope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            }
                        }
                    },
                    onVerticalDrag = { _, dragAmount ->
                        currentY = (currentY + dragAmount).coerceIn(0f, sliderSize.height.toFloat())
                        val letter = yToLetter(currentY.toInt(), sliderSize.height)
                        if (letter != null && letterToIndex.containsKey(letter)) {
                            selectedLetter = letter
                            letterToIndex[letter]?.let { index ->
                                scope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            }
                        }
                    },
                    onDragEnd = { selectedLetter = null },
                )
            }
            .padding(vertical = 24.dp, horizontal = 4.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            LETTERS.forEach { letter ->
                val isSelected = selectedLetter == letter
                val hasApps = letterToIndex.containsKey(letter)
                val letterScale = if (isSelected) scale else 1f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 20.dp)
                        .clickable(enabled = hasApps) {
                            if (hasApps) {
                                selectedLetter = letter
                                letterToIndex[letter]?.let { index ->
                                    scope.launch {
                                        listState.animateScrollToItem(index)
                                        delay(350)
                                        selectedLetter = null
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Text(
                        text = letter.toString(),
                        style = AppTypography.caption(),
                        color = when {
                            isSelected -> accentColors.primary
                            hasApps -> textSecondary
                            else -> textSecondary.copy(alpha = 0.35f)
                        },
                        modifier = Modifier
                            .scale(letterScale)
                            .size(if (isSelected) 28.dp else 20.dp),
                    )
                }
            }
        }
    }
}

private fun yToLetter(y: Int, height: Int): Char? {
    if (height <= 0) return null
    val ratio = y.toFloat() / height
    val index = (ratio * LETTERS.size).toInt().coerceIn(0, LETTERS.size - 1)
    return LETTERS[index]
}
