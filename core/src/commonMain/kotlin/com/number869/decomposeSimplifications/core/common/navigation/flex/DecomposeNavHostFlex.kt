package com.number869.decomposeSimplifications.core.common.navigation.flex

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.number869.decomposeSimplifications.core.common.ultils.LocalDecomposeComponentContext
import com.number869.decomposeSimplifications.core.common.ultils.animation.OverlayStackNavigationAnimation

@ExperimentalDecomposeApi
@Composable
fun DecomposeNavHostFlex(
    navController: DecomposeNavControllerFlex,
    modifier: Modifier = Modifier,
    nonOverlayDefaultAnimation: StackAnimation<String, DecomposeChildInstanceFlex> = predictiveBackAnimation(
        backHandler = navController::backHandler.get(),
        fallbackAnimation = stackAnimation { child ->
            navController.animationsForDestinations[child.configuration]
        },
        onBack = navController::navigateBack
    ),
    overlayDefaultAnimation: StackAnimation<String, DecomposeChildInstanceFlex> = OverlayStackNavigationAnimation { child ->
        navController.animationsForDestinations[child.configuration]
    },
    snackDefaultAnimation: StackAnimation<String, DecomposeChildInstanceFlex> = OverlayStackNavigationAnimation { child ->
        navController.animationsForDestinations[child.configuration]
    },
    startingPoint: StartingDestination,
    content: @Composable (nonOverlayContent: @Composable (modifier: Modifier) -> Unit) -> Unit
) {
    if (navController.currentScreenDestination == "empty") {
        navController.openStartingScreen(startingPoint)
    }

    Box {
        // not overlay
        content.invoke { nonOverlayModifier ->
            Children(
                navController.screenStack,
                nonOverlayModifier,
                nonOverlayDefaultAnimation
            ) {
                CompositionLocalProvider(
                    LocalDecomposeComponentContext provides it.instance.componentContext
                ) {
                    navController.contentOfScreens[it.configuration]?.invoke()
                }
            }
        }

        // overlay
        Children(navController.overlayStack, modifier, overlayDefaultAnimation) {
            CompositionLocalProvider(
                LocalDecomposeComponentContext provides it.instance.componentContext
            ) {
                navController.contentOfOverlays[it.configuration]?.invoke()
            }
        }

        // overlay, but snacks
        Children(navController.snackStack, animation = snackDefaultAnimation) {
            CompositionLocalProvider(
                LocalDecomposeComponentContext provides it.instance.componentContext
            ) {
                navController.contentOfSnacks[it.configuration]?.invoke()

                DisposableEffect(it) {
                    onDispose { navController.removeSnackContents(it.configuration) }
                }
            }
        }
    }
}