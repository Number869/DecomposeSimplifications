package com.number869.decomposeSimplifications.core.common

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.serialization.builtins.serializer

fun decomposeNavControllerFlex(
    componentContext: DefaultComponentContext? = null
): DecomposeNavControllerFlex = DecomposeNavControllerFlex(
    componentContext ?: DefaultComponentContext(
        LifecycleRegistry(),
        StateKeeperDispatcher(savedState = tryRestoreStateFromFile())
    )
)

class DecomposeNavControllerFlex(
    val componentContext: DefaultComponentContext
) : ComponentContext by componentContext {
    private val screenNavigation = StackNavigation<String>()
    private val overlayNavigation = StackNavigation<String>()

    val contentOfScreens = componentContext.instanceKeeper.getOrCreate {
        ScreensInstanceHolder(mutableStateMapOf<String, @Composable () -> Unit>())
    }.data

    val contentOfOverlays = componentContext.instanceKeeper.getOrCreate {
        OverlaysInstanceHolder(mutableStateMapOf<String, @Composable () -> Unit>())
    }.data

    val animationsForDestinations = componentContext.instanceKeeper.getOrCreate {
        AnimationsInstanceHolder(mutableMapOf<String, StackAnimator>())
    }.data

    val screenStack = childStack(
        source = screenNavigation,
        serializer = String.serializer(),
        initialConfiguration = "empty",
        key = "screenStack",
        handleBackButton = true,
        childFactory = { config, componentContext ->
            DecomposeChildInstanceFlex(config, componentContext)
        }
    )

    val overlayStack = childStack(
        source = overlayNavigation,
        serializer = String.serializer(),
        initialConfiguration = "empty",
        key = "overlayStack",
        handleBackButton = true,
        childFactory = { config, componentContext ->
            DecomposeChildInstanceFlex(config, componentContext)
        }
    )

    val currentScreenDestination
        @Composable
        get() = screenStack.subscribeAsState().value.active.configuration

    val currentOverlayDestination
        @Composable
        get() = overlayStack.subscribeAsState().value.active.configuration

    fun openStartingScreen(startingDestination: StartingDestination) {
        contentOfScreens[startingDestination.key] = startingDestination.content
        screenNavigation.replaceCurrent(startingDestination.key)
    }

    fun openAsScreen(
        key: String,
        animation: StackAnimator? = fade(tween(200)) + scale(tween(200)),
        content: @Composable () -> Unit
    ) {
        // remember data about content
        animation?.let { animationsForDestinations[key] = it }
        contentOfScreens[key] = content

        overlayNavigation.replaceAll("empty")

        // navigate
        if (screenStack.items.find { it.configuration == key } == null)
            screenNavigation.push(key)
        else
            screenNavigation.popTo(screenStack.items.indexOfLast { it.configuration == key })
    }

    fun openInOverlay(
        key: String,
        animation: StackAnimator? = fade(tween(200)),
        content: @Composable BoxScope.() -> Unit
    ) {
        // remember data about content
        animation?.let { animationsForDestinations[key] = it }
        contentOfOverlays[key] = { Box(content = content, modifier = Modifier.fillMaxSize()) }

        // navigate
        overlayNavigation.push(key)
    }

    fun navigateBack(onComplete: (isSuccess: Boolean) -> Unit = {}) {
        var thingToRemove: String? = ""
        if (overlayStack.active.configuration == "empty") {
            thingToRemove = screenStack.active.configuration

            screenNavigation.pop() {
                onComplete(it)

                contentOfScreens.remove(thingToRemove)
                animationsForDestinations.remove(thingToRemove)
            }
        } else {
            thingToRemove = overlayStack.active.configuration
            overlayNavigation.pop() {
                onComplete(it)

                contentOfOverlays.remove(thingToRemove)
                animationsForDestinations.remove(thingToRemove)
            }
        }
    }
}

data class StartingDestination(
    val key: String,
    val content: @Composable () -> Unit
)

class DecomposeChildInstanceFlex(
    val config: String,
    val componentContext: ComponentContext
)

private data class ScreensInstanceHolder<T>(val data: T) : InstanceKeeper.Instance
private data class OverlaysInstanceHolder<T>(val data: T) : InstanceKeeper.Instance
private data class AnimationsInstanceHolder<T>(val data: T) : InstanceKeeper.Instance