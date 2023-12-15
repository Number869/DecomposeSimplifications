package com.number869.decomposeSimplifications.common

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import com.number869.decomposeSimplifications.core.common.LocalDecomposeComponentContext
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

        LaunchedEffect(null) {
            animate(currentOffset, initialOffset) { value, _ -> currentOffset = value }
        }

        var dismissalWasRequested by remember { mutableStateOf(false) }

        fun closeThis() {
            coroutineScope.launch {
                animate(
                    initialValue = currentOffset,
                    targetValue = screenSize.height,
                ) { value, _ ->
                    currentOffset = value

                    // check each value because the bottom sheet can be caught while
                    // this is being executed. putting onDismiss() after animate() would result
                    // in the sheet closing anyway if the user caught it
                    if (value >= screenSize.height && !dismissalWasRequested) {
                        onDismiss()
                        dismissalWasRequested = true
                    }
                }
            }
        }

        LocalDecomposeComponentContext.current.backHandler.register(
            BackCallback { coroutineScope.launch { closeThis() } }
        )

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset = when {
                    available.y >= 0 || currentOffset <= 0f -> Offset.Zero

                    else -> {
                        currentOffset += available.y

                        available
                    }
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset = when {
                    available.y <= 0 || currentOffset.coerceAtMost(maxOffset) == 0f -> Offset.Zero

                    else -> {
                        currentOffset += available.y

                        available
                    }
                }

                override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                    coroutineScope.launch {
                        val isFullScreen = currentOffset == minOffset
                        val speedThreshold = if (isFullScreen) 1500f else 15f

                        when {
                            available.y > speedThreshold || currentOffset > screenSize.height / 2 -> {
                                closeThis()
                            }

                            else -> {
                                val targetOffset = if (available.y < -20f || currentOffset < initialOffset)
                                    minOffset
                                else
                                    initialOffset

                                animate(
                                    initialValue = currentOffset,
                                    targetValue = targetOffset,
                                    initialVelocity = available.y
                                ) { value, _ ->
                                    currentOffset = value
                                }
                            }
                        }
                    }

                    return super.onPostFling(consumed, available)
                }
            }
        }

        Box(
            Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = ::closeThis
                )
                .graphicsLayer { alpha = 0.48f * (1f - (currentOffset / maxOffset)).coerceIn(0f, 1f) }
                .fillMaxSize()
                .background(Color.Black)
        )

        Box(
            modifier
                .nestedScroll(nestedScrollConnection)
                .graphicsLayer { translationY = currentOffset.coerceAtLeast(0f) }
                .heightIn(maxHeight)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}