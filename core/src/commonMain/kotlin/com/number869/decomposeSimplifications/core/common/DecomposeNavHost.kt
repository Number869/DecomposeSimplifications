package com.number869.decomposeSimplifications.core.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.Child
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
    val childWithCorrectType = it as? Child.Created<C, DecomposeChildInstance<C>>

    content(
        childWithCorrectType!!.instance.config,
        childWithCorrectType.instance.componentContext,
        childWithCorrectType.instance
    )
}