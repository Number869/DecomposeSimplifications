package com.number869.decomposeSimplifications.core.common.navigation.standard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.*
import com.number869.decomposeSimplifications.core.common.ultils.LocalDecomposeComponentContext

/**
 * Basic nav host. Provides the child's [ComponentContext] using [CompositionLocalProvider],
 * so in your screens you can retrieve it easily by calling [LocalDecomposeComponentContext].currentю
 * [LocalDecomposeComponentContext] is also used for [decomposeViewModel] usage
 * inside the screens *by default*.
 */
@Composable
fun <C : Any> DecomposeNavHost(
    navController: DecomposeNavController<C>,
    modifier: Modifier = Modifier,
    animation: StackAnimation<C, DecomposeChildInstance<C>> = stackAnimation(scale() + fade()),
    content: @Composable (
        destination: C,
        componentContext: ComponentContext,
        instance: DecomposeChildInstance<C>
    ) -> Unit
) = Children(
    navController.stack,
    modifier,
    animation
) {
    CompositionLocalProvider(
        LocalDecomposeComponentContext provides it.instance.componentContext
    ) {
        content(
            it.instance.config,
            it.instance.componentContext,
            it.instance
        )
    }
}