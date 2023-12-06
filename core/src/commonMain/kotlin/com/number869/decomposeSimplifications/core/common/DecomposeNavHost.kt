package com.number869.decomposeSimplifications.core.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.*

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