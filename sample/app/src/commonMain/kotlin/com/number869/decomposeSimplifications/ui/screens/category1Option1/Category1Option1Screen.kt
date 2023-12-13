package com.number869.decomposeSimplifications.ui.screens.category1Option1

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.arkivanov.essenty.backhandler.BackCallback
import com.number869.decomposeSimplifications.core.common.DecomposeNavControllerFlex
import com.number869.decomposeSimplifications.core.common.LocalDecomposeComponentContext
import com.number869.decomposeSimplifications.core.common.decomposeViewModel
import com.number869.decomposeSimplifications.ui.navigation.Screens
import com.number869.decomposeSimplifications.ui.screens.category2default.Category2DefaultScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Category1Option1Screen(id: String, navController: DecomposeNavControllerFlex) {
    val vm = decomposeViewModel(Category1Option1ViewModel())
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Category 1 Option 1 $id")
        Text("random uuid from vm that destroys itself after exit ${vm.randomUuid}")
        FilledTonalButton(
            onClick = {
                navController.openAsScreen(Screens.Category2.Default.destinationName) {
                    Category2DefaultScreen(navController)
                }
            }
        ) {
            Text("Open category 2")
        }

        FilledTonalButton(
            onClick = {
                // bottom sheets don't need fade animation (that's set by default)
                navController.openInOverlay(key = "meoooow", animation = null) {
                    MeowBottomsheet(navController)
                }
            }
        ) {
            Text("Open bottom sheet")
        }
    }
}

@Composable
fun MeowBottomsheet(navController: DecomposeNavControllerFlex) {
    val density = LocalDensity.current.density
    val coroutineScope = rememberCoroutineScope()
    val initialOffset = 900f * density
    var verticalOffset by remember { mutableStateOf(initialOffset) }
    var gestureBeginningOffset by remember { mutableStateOf(0f) }

    val draggableState = rememberDraggableState { verticalOffset += it  }

    val componentContext = LocalDecomposeComponentContext.current
    componentContext.backHandler.register(
        BackCallback {
            coroutineScope.launch {
                animate(verticalOffset, initialOffset) { value, _ ->
                    verticalOffset = value
                }
                navController.navigateBack()
            }
        }
    )

    Box(
        Modifier
            .graphicsLayer { alpha = 1f - (verticalOffset / initialOffset).coerceIn(0f, 1f)  }
            .fillMaxSize()
            .background(Color.Black.copy(0.25f))
    )

    Box(
        Modifier
            .draggable(
                draggableState,
                Orientation.Vertical,
                onDragStarted = { gestureBeginningOffset = it.y },
                onDragStopped = { gestureVelocity ->
                    // if upwards
                    if (gestureVelocity < 500f) {
                        animate(verticalOffset, 100f, gestureVelocity) { value, _ ->
                            verticalOffset = value
                        }
                    // if downwards
                    } else if (gestureVelocity > 500f) {
                        animate(verticalOffset, initialOffset, gestureVelocity) { value, _ ->
                            verticalOffset = value
                        }
                        navController.navigateBack()
                    } else {
                        animate(verticalOffset, gestureBeginningOffset, gestureVelocity) { value, _ ->
                            verticalOffset = value
                        }
                    }
                }
            )
            .graphicsLayer { translationY = verticalOffset }
            .shadow(4.dp)
            .height(900.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Button(
            modifier = Modifier.align(Alignment.Center),
            onClick = {
                navController.openInOverlay("meow dialog") { MeowDialog(navController) }
            }
        ) {
            Text("Open a dialog")
        }
    }

    LaunchedEffect(null) {
        animate(verticalOffset, 600f) { value, velocity ->
            verticalOffset = value
        }
    }
}

@Composable
fun MeowDialog(navController: DecomposeNavControllerFlex) {
    AlertDialog(
        onDismissRequest = navController::navigateBack,
        text = { Text("another dialooooog :3333") },
        confirmButton = {
            Button(onClick = navController::navigateBack) {
                Text("Close")
            }
        }
    )
}