package com.number869.decomposeSimplifications.core.common.navigation.alt

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.number869.decomposeSimplifications.core.common.ultils.LocalDecomposeComponentContext
import com.number869.decomposeSimplifications.core.common.ultils.animation.OverlayStackNavigationAnimation

/**
 * Alternative nav host. Inspired by Google's Jetpack Compose Navigation and
 * safer-navigation-compose by uragiristereo (on GitHub). Provides a space for normal
 * content and overlay's, as well as custom snack/toast message ui's.
 */
@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun <T : Any> DecomposeAltNavHost(
    navController: DecomposeAltNavController<T>,
    startingDestination: T,
    screenContainer: @Composable (
        containerContent: @Composable (modifier: Modifier) -> Unit
    ) -> Unit,
    content: @Composable DecomposeAltNavHostScope<T>.() -> Unit,
) {
    val scope = remember { DecomposeAltNavHostScope<T>() }

    if (navController.currentScreenDestination == navController.emptyDestination) {
        navController.setStartingScreen(startingDestination)
    }

    with(scope) {
        Box {
            screenContainer { screenContainerModifier ->
                Children(
                    navController.screenStack,
                    screenContainerModifier,
                    predictiveBackAnimation(
                        backHandler = navController::backHandler.get(),
                        onBack = navController::navigateBack,
                        fallbackAnimation = stackAnimation { child ->
                            animationsHolder[child.configuration::class]
                        }
                    )
                ) {
                    CompositionLocalProvider(
                        LocalDecomposeComponentContext provides it.instance.componentContext,
                        LocalContentType provides ContentTypes.Screen,
                        LocalDecomposeConfiguration provides it.configuration
                    ) {
                        content()
                    }
                }
            }

            Children(
                navController.overlayStack,
                animation = predictiveBackAnimation(
                    backHandler = navController::backHandler.get(),
                    onBack = navController::navigateBack,
                    fallbackAnimation = OverlayStackNavigationAnimation { child ->
                        animationsHolder[child.configuration::class]
                    }
                )
            ) {
                CompositionLocalProvider(
                    LocalDecomposeComponentContext provides it.instance.componentContext,
                    LocalContentType provides ContentTypes.Overlay,
                    LocalDecomposeConfiguration provides it.configuration
                ) {
                    content()
                }
            }

            Children(
                navController.snackStack,
                animation = stackAnimation { child ->
                    navController.animationsForDestinations[child.configuration]
                }
            ) {
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
}