package com.number869.decomposeSimplifications.common

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import com.arkivanov.essenty.backhandler.BackCallback
import com.number869.decomposeSimplifications.core.common.ultils.LocalDecomposeComponentContext
import kotlinx.coroutines.launch

@Composable
fun BasicBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    BoxWithConstraints {
        val density = LocalDensity.current.density
        val screenSize = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        val coroutineScope = rememberCoroutineScope()

        val initialOffset = screenSize.height * 0.4f
        val minOffset = WindowInsets.statusBars
            .asPaddingValues()
            .calculateTopPadding()
            .value * density
        val maxOffset = screenSize.height - minOffset

        var currentOffset by remember { mutableStateOf(screenSize.height) }

        var dismissalWasRequested by remember { mutableStateOf(false) }

        fun animateSheetOffset(targetOffset: Float, velocity: Float? = null, onEnd: () -> Unit = {}) {
            coroutineScope.launch {
                animate(currentOffset, targetOffset, velocity ?: 0f) { value, _ ->
                    currentOffset = value

                    if (value >= screenSize.height) onEnd()
                }
            }
        }

        fun close() = animateSheetOffset(
            screenSize.height,
            onEnd = {
                if (!dismissalWasRequested) {
                    onDismiss()
                    dismissalWasRequested = true
                }
            }
        )

        LaunchedEffect(null) { animateSheetOffset(initialOffset) }

        LocalDecomposeComponentContext.current.backHandler.register(
            BackCallback() { close() }
        )

        // i little idea about how this works
        val sheetScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    return when {
                        currentOffset <= minOffset -> Offset.Zero

                        else -> {
                            // update offset making sure the sheet doesn't go under the status bar
                            currentOffset = (currentOffset + available.y).coerceAtLeast(minOffset)

                            available
                        }
                    }
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    val verticalSpeed = available.y

                    currentOffset += verticalSpeed

                    // if sheet is expanded and displayed under the status bar -
                    // animate back to minimum offset when gesture ends
                    return if (source == NestedScrollSource.Fling && currentOffset <= 0f) {
                        animateSheetOffset(minOffset, available.y)

                        // stop nested scroll connection from telling the ui to scroll
                        // to correctly animate the sheet offset
                        Offset.Zero
                    } else {
                        // this "if" fixes overscroll effect when the sheet is under the status bar
                        if (currentOffset >= 0f) available else Offset.Zero
                    }
                }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    val verticalSpeed = available.y

                    coroutineScope.launch {
                        val speedThreshold = if (currentOffset == minOffset) 1500f else 15f

                        val gestureDownwardsWasFast = verticalSpeed > speedThreshold
                        val isInTheLowerHalfOfTheScreen = currentOffset > screenSize.height / 2

                        when {
                            gestureDownwardsWasFast || isInTheLowerHalfOfTheScreen -> close()

                            else -> {
                                val targetOffset = if (verticalSpeed < -20f || currentOffset < initialOffset)
                                    minOffset // expand
                                else
                                    initialOffset // collapse, but not entirely

                                animateSheetOffset(targetOffset, verticalSpeed)
                            }
                        }
                    }

                    return super.onPostFling(consumed, available)
                }
            }
        }

        // clickable scrim
        Box(
            Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = ::close
                )
                .graphicsLayer { alpha = 0.48f * (1f - (currentOffset / maxOffset)).coerceIn(0f, 1f) }
                .fillMaxSize()
                .background(Color.Black)
        )

        Box (
            modifier
                .graphicsLayer { translationY = currentOffset.coerceAtLeast(0f) }
                .nestedScroll(sheetScrollConnection)
                .verticalScroll(rememberScrollState())
                .heightIn(max = maxHeight)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}