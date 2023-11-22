import androidx.compose.runtime.*
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.parcelable.ParcelableContainer
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import java.io.File
import java.io.ObjectInputStream

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
        startingDestination,
    )
}

class DecomposeNavController<C : Any>(
    componentContext: DefaultComponentContext,
    startingDestination: C
) : ComponentContext by componentContext {
    private val navigation = StackNavigation<C>()

    @OptIn(InternalSerializationApi::class)
    private val serialzer = serializer(startingDestination.javaClass) as KSerializer<C>

    val stack = childStack(
        source = navigation,
        serializer = serialzer,
        initialConfiguration = startingDestination,
        handleBackButton = true,
        childFactory = ::child
    )

    private var _currentDestination by mutableStateOf(stack.active.instance.config)
    val currentDestination get() = _currentDestination

    fun onBackClicked(toIndex: Int) {
        navigation.popTo(index = toIndex)
        _currentDestination = stack.active.instance.config
    }

    private fun child(
        config: C,
        componentContext: ComponentContext
    ): DecomposeChildInstance<C> = DecomposeChildInstance(config, componentContext)


    fun navigate(targetDestination: C) {
        navigation.bringToFront(targetDestination)
        _currentDestination = stack.active.instance.config
    }

    fun pop(onComplete: (isSuccess: Boolean) -> Unit = {}) {
        navigation.pop(onComplete)
        _currentDestination = stack.active.instance.config
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