package com.number869.decomposeSimplifications.core.common.navigation.alt

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
import com.number869.decomposeSimplifications.core.common.ultils.tryRestoreStateFromFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun <T: Any> decomposeAltNavController(
    emptyDestination: T,
    serializer: KSerializer<T>,
    componentContext: DefaultComponentContext? = null
): DecomposeAltNavController<T> = DecomposeAltNavController(
    emptyDestination = emptyDestination,
    componentContext = componentContext ?: DefaultComponentContext(
        LifecycleRegistry(),
        StateKeeperDispatcher(savedState = tryRestoreStateFromFile())
    ),
    serializer = serializer
)

class DecomposeAltNavController<T : Any>(
    val emptyDestination: T,
    componentContext: DefaultComponentContext,
    serializer: KSerializer<T>
) : ComponentContext by componentContext {
    private val scope = MainScope()
    private val mutex = Mutex()

    private val screenNavigation = StackNavigation<T>()
    private val overlayNavigation = StackNavigation<T>()
    private val snackNavigation = StackNavigation<String>()

    val contentOfSnacks = componentContext.instanceKeeper.getOrCreate {
        SnacksContentHolder(mutableStateMapOf<String, @Composable () -> Unit>())
    }.data

    val animationsForDestinations = componentContext.instanceKeeper.getOrCreate {
        AnimationsHolder(mutableMapOf<String, StackAnimator>())
    }.data

    val screenStack = childStack(
        source = screenNavigation,
        serializer = serializer,
        initialConfiguration = emptyDestination,
        key = "screenStack",
        handleBackButton = true,
        childFactory = { config, context -> DecomposeChildInstanceAlt(config, context) }
    )

    val overlayStack = childStack(
        source = overlayNavigation,
        serializer = serializer,
        initialConfiguration = emptyDestination,
        key = "overlayStack",
        handleBackButton = true,
        childFactory = { config, context -> DecomposeChildInstanceAlt(config, context) }
    )

    val snackStack = childStack(
        source = snackNavigation,
        serializer = String.serializer(),
        initialConfiguration = "empty",
        key = "snackStack",
        handleBackButton = false,
        childFactory = { config, context -> DecomposeChildInstanceAlt(config, context) }
    )

    fun navigateToScreen(destination: T) {
        screenNavigation.bringToFront(destination)

        // clear the overlay stack
        overlayNavigation.replaceAll(emptyDestination)
    }
    fun <D> closeScreen(destination: D, onComplete: (isSuccess: Boolean) -> Unit = { }) {
        val stackWithoutThisKeyAsArrayOfKeys = screenStack.backStack
            .filterNot { it.configuration as D == destination }
            .map { it.configuration as Any }
            .toTypedArray()

        screenNavigation.replaceAll(*stackWithoutThisKeyAsArrayOfKeys as Array<T>) {
            onComplete(true)
        }
    }

    fun navigateToOverlay(destination: T) {
        overlayNavigation.bringToFront(destination)
    }

    fun <D> closeOverlay(destination: D, onComplete: (isSuccess: Boolean) -> Unit = { }) {
        val stackWithoutThisKeyAsArrayOfKeys = overlayStack.backStack
            .filterNot { it.configuration as D == destination }
            .map { it.configuration as Any }
            .toTypedArray()

        overlayNavigation.replaceAll(*stackWithoutThisKeyAsArrayOfKeys as Array<T>) {
            onComplete(true)
        }
    }

    fun navigateBack(onComplete: (isSuccess: Boolean) -> Unit = { }) {
        if (overlayStack.active.configuration == emptyDestination) {

            screenNavigation.pop() {
                onComplete(it)
            }
        } else {
            overlayNavigation.pop() {
                onComplete(it)
            }
        }
    }

    fun setStartingScreen(startingDestination: T) {
        screenNavigation.replaceAll(startingDestination)
    }

    fun openInSnack(
        key: String = UUID.randomUUID().toString(),
        animation: StackAnimator? = fade(tween(200)) + scale(tween(200)),
        displayDurationMillis: Duration = 5.seconds,
        content: @Composable BoxScope.() -> Unit
    ) {
        scope.launch(Dispatchers.Main) {
            mutex.withLock {
                // remember data about content. the content is removed from within
                // the nav host using DisposableEffect for proper animations using
                animation?.let { animationsForDestinations[key] = it }
                contentOfSnacks[key] = { Box(content = content, modifier = Modifier.fillMaxSize()) }

                snackNavigation.push(key)
                delay(displayDurationMillis)
                closeSnack(key)

                // delay before displaying another snack
                delay(150)
            }
        }
    }

    fun removeSnackContents(key: String) {
        contentOfSnacks.remove(key)
        animationsForDestinations.remove(key)
    }

    fun closeSnack(key: String, onComplete: (isSuccess: Boolean) -> Unit = { }) {
        val stackWithoutThisKeyAsArrayOfKeys = snackStack.backStack
            .filterNot { it.configuration == key }
            .map { it.configuration }
            .toTypedArray()

        snackNavigation.replaceAll(*stackWithoutThisKeyAsArrayOfKeys) {
            onComplete(true)
        }
    }

    val currentScreenDestination
        @Composable
        get() = screenStack.subscribeAsState().value.active.configuration

    val currentOverlayDestination
        @Composable
        get() = overlayStack.subscribeAsState().value.active.configuration
}


private data class SnacksContentHolder<T>(val data: T) : InstanceKeeper.Instance
private data class AnimationsHolder<T>(val data: T) : InstanceKeeper.Instance

data class DecomposeChildInstanceAlt<C>(val config: C, val componentContext: ComponentContext)