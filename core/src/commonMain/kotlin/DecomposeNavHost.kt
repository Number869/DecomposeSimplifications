import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.StackAnimation
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.parcelable.ParcelableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import java.io.File
import java.io.ObjectInputStream

@Composable
fun <C : Any, T : Any> DecomposeNavHost(
    navController: DecomposeNavController<C>,
    modifier: Modifier = Modifier,
    animation: StackAnimation<C, T>,
    content: @Composable (
        destination: C,
        componentContext: ComponentContext,
        instance: DecomposeChildInstance<C>
    ) -> Unit
) = Children(
    navController.stack as Value<ChildStack<C, T>>,
    modifier,
    animation
) {
    val childWithCorrectType = it as Child.Created<*, DecomposeChildInstance<C>>

    content(
        childWithCorrectType.instance.config,
        childWithCorrectType.instance.componentContext,
        childWithCorrectType.instance
    )
}

/**
 * Pass the component context on android pls :3
 */
@Composable
fun <C : Any> rememberDecomposeNavController(
    startingDestination: C,
    componentContext: DefaultComponentContext? = null
): DecomposeNavController<C> = remember {
    DecomposeNavController(
        componentContext ?: DefaultComponentContext(
            LifecycleRegistry(),
            StateKeeperDispatcher(savedState = tryRestoreStateFromFile())
        ),
        startingDestination
    )
}

class DecomposeNavController<C : Any>(
    componentContext: DefaultComponentContext,
    startingDestination: C,
) : ComponentContext by componentContext {
    private val navigation = StackNavigation<C>()

    val stack = childStack(
        source = navigation,
        serializer = null,
        initialConfiguration = startingDestination,
        handleBackButton = true,
        childFactory = ::child,
    ) as Value<ChildStack<*, DecomposeChildInstance<C>>>

    var currentDestination by mutableStateOf(stack.active.instance.config)

    private fun child(
        config: C,
        componentContext: ComponentContext
    ): DecomposeChildInstance<C> = DecomposeChildInstance(config, componentContext)

    fun onBackClicked(toIndex: Int) {
        navigation.popTo(index = toIndex)
        currentDestination = stack.active.instance.config
    }

    fun navigate(targetDestination: C) {
        navigation.bringToFront(targetDestination)
        currentDestination = stack.active.instance.config
    }

    fun pop(onComplete: (isSuccess: Boolean) -> Unit = {}) {
        navigation.pop(onComplete)
        currentDestination = stack.active.instance.config
    }
}

class DecomposeChildInstance<C>(val config: C, val componentContext: ComponentContext)

private fun tryRestoreStateFromFile(): ParcelableContainer? {
    return File("states.dat").takeIf(File::exists)?.let { file ->
        try {
            ObjectInputStream(file.inputStream()).use(ObjectInputStream::readObject) as ParcelableContainer
        } catch (e: Exception) {
            null
        } finally {
            file.delete()
        }
    }
}