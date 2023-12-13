package com.number869.decomposeSimplifications.core.common

import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.serialization.KSerializer

/**
 * It's recommended to use [decomposeNavController] instead.
 * Pass the component context on android pls :3
 */
@Deprecated("It's recommended to use decomposeNavController instead to avoid issues with saved state handling")
@Composable
fun <C : Any> rememberDecomposeNavController(
    startingDestination: C,
    componentContext: DefaultComponentContext? = null,
    serializer: KSerializer<C>? = null
): DecomposeNavController<C> = remember {
    DecomposeNavController(
        componentContext ?: DefaultComponentContext(
            LifecycleRegistry(),
            StateKeeperDispatcher(savedState = tryRestoreStateFromFile())
        ),
        startingDestination,
        serializer = serializer
    )
}

/**
 * Initialize this outside of setContent in the activity and then just pass it down inside
 * your nav host and everywhere else. Pass the component context on android pls :3
 */
fun <C : Any> decomposeNavController(
    startingDestination: C,
    componentContext: DefaultComponentContext? = null,
    serializer: KSerializer<C>? = null
): DecomposeNavController<C> = DecomposeNavController(
    componentContext ?: DefaultComponentContext(
        LifecycleRegistry(),
        StateKeeperDispatcher(savedState = tryRestoreStateFromFile())
    ),
    startingDestination,
    serializer
)


class DecomposeNavController<C : Any>(
    val componentContext: DefaultComponentContext,
    startingDestination: C,
    serializer: KSerializer<C>?
) : ComponentContext by componentContext {
    private val navigation = StackNavigation<C>()

    val stack = childStack(
        source = navigation,
        serializer = serializer,
        initialConfiguration = startingDestination,
        handleBackButton = true,
        childFactory = { config, componentContext -> DecomposeChildInstance(config, componentContext) }
    )

    private var _currentDestination by mutableStateOf(stack.active.instance.config)
    val currentDestination get() = _currentDestination

    fun onBackClicked(toIndex: Int) {
        navigation.popTo(index = toIndex)
        _currentDestination = stack.active.configuration
    }

    fun navigate(targetDestination: C) {
        navigation.bringToFront(targetDestination)
        _currentDestination = stack.active.configuration
    }

    fun pop(onComplete: (isSuccess: Boolean) -> Unit = {}) {
        navigation.pop(onComplete)
        _currentDestination = stack.active.configuration
    }
}

class DecomposeChildInstance<C>(val config: C, val componentContext: ComponentContext)