package com.number869.decomposeSimplifications.core.common

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.extensions.compose.stack.animation.Direction
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.router.stack.ChildStack

internal abstract class OverlayOrientedAbstractStackAnimation<C : Any, T : Any>(
    private val disableInputDuringAnimation: Boolean,
) : StackAnimation<C, T> {
    private val items = mutableStateMapOf<C, AnimationItem<C, T>>()

    @Composable
    protected abstract fun Child(
        item: AnimationItem<C, T>,
        onFinished: () -> Unit,
        content: @Composable (child: Child.Created<C, T>) -> Unit,
    )

    @Composable
    override operator fun invoke(stack: ChildStack<C, T>, modifier: Modifier, content: @Composable (child: Child.Created<C, T>) -> Unit) {
        var currentStack by remember { mutableStateOf(stack) }

        if (stack.active.configuration != currentStack.active.configuration) {
            val oldStack = currentStack
            currentStack = stack
            getAndSetAnimationItems(newStack = currentStack, oldStack = oldStack)
        }

        Box(modifier = modifier) {
            items.forEach { (configuration, item) ->
                key(configuration) {
                    Child(
                        item = item,
                        onFinished = {
                            if (!currentStack.items.contains(item.child.configuration)) {
                                items.remove(item.child.configuration)
                            }
                        },
                        content = content,
                    )
                }
            }

            // A workaround until https://issuetracker.google.com/issues/214231672.
            // Normally only the exiting child should be disabled.
            if (disableInputDuringAnimation && (items.size > 1)) {
                Overlay(modifier = Modifier.matchParentSize())
            }
        }
    }

    @Composable
    private fun Overlay(modifier: Modifier) {
        Box(
            modifier = modifier.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
        )
    }

    private fun getAndSetAnimationItems(newStack: ChildStack<C, T>, oldStack: ChildStack<C, T>?): Map<C, AnimationItem<C, T>> {
        when {
            oldStack == null -> {
                items[newStack.active.configuration] = AnimationItem(
                    child = newStack.active,
                    direction = Direction.ENTER_FRONT,
                    isInitial = true
                )
            }

            (newStack.size < oldStack.size) && (newStack.active.configuration in oldStack.backStack) -> {
                items[oldStack.active.configuration] = AnimationItem(
                    child = oldStack.active,
                    direction = Direction.EXIT_FRONT,
                    otherChild = newStack.active
                )
            }

            else -> {
                items[oldStack.active.configuration] = AnimationItem(
                    child = oldStack.active,
                    direction = Direction.ENTER_FRONT,
                    otherChild = newStack.active
                )

                items[newStack.active.configuration] = AnimationItem(
                    child = newStack.active,
                    direction = Direction.ENTER_FRONT,
                    otherChild = oldStack.active
                )
            }
        }

        return items
    }

    private val ChildStack<*, *>.size: Int
        get() = items.size

    private operator fun <C : Any> Iterable<Child<C, *>>.contains(config: C): Boolean =
        any { it.configuration == config }

    data class AnimationItem<out C : Any, out T : Any>(
        val child: Child.Created<C, T>,
        val direction: Direction,
        val isInitial: Boolean = false,
        val otherChild: Child.Created<C, T>? = null,
    )
}