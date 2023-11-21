import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.StackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value

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
    (navController.stack as Value<ChildStack<C, T>,>).value,
    modifier,
    animation
) {
    val childWithCorrectType = it as? Child.Created<*, DecomposeChildInstance<C>>

    runCatching {
        content(
            childWithCorrectType!!.instance.config,
            childWithCorrectType.instance.componentContext,
            childWithCorrectType.instance
        )
    }.onFailure { exception ->
        throw exception
    }
}