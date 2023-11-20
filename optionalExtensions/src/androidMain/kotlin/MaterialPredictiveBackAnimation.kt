@file:OptIn(ExperimentalDecomposeApi::class)

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.*
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackHandler
import kotlinx.coroutines.launch

fun <C : Any, T : Any> materialPredictiveBackAnimation(
    backHandler: BackHandler,
    animation: StackAnimation<C, T> = stackAnimation(fade() + scale()),
    windowWidthDp: Int,
    onBack: () -> Unit,
    densityProvider: Density,
    selector: (
        initialBackEvent: BackEvent,
        exitChild: Child.Created<C, T>,
        enterChild: Child.Created<C, T>
    ) -> PredictiveBackAnimatable = { initialBackEvent, exitChild, enterChild ->
        MaterialPredictiveBackAnimatable(initialBackEvent, windowWidthDp, densityProvider)
    },
): StackAnimation<C, T> = PredictiveBackAnimation(
    backHandler,
    animation,
    selector,
    onBack
)

@OptIn(ExperimentalDecomposeApi::class)
class MaterialPredictiveBackAnimatable(
    initialBackEvent: BackEvent,
    windowWidthDp: Int,
    private val densityProvider: Density
) : PredictiveBackAnimatable {

    private val progressAnimatable = Animatable(initialValue = 1F)
    private var gestureData by mutableStateOf(initialBackEvent)

    private var gestureOffsetWhenStarted by mutableStateOf(Offset.Zero)

    private val gestureSwipeDistanceOffsetY = { gestureData.touchY - gestureOffsetWhenStarted.y }

    // when progressAnimatable is 0 - the offset will be also
    // 0, aka the default
    private var processedOffset = {
        Offset(
            x = if (gestureData.swipeEdge == BackEvent.SwipeEdge.LEFT)
                (((windowWidthDp * 0.05f) - 8) * gestureData.progress) * densityProvider.density
            else
                -(((windowWidthDp * 0.05f) - 8) * gestureData.progress) * densityProvider.density,
            y = (((gestureSwipeDistanceOffsetY() * 0.05f) - 8)
                    // limit vertical offset movement if gesture progress is close to 0
                    // once gesture progress is at 30% - max vertical movement
                    * (gestureData.progress * 3).coerceAtMost(1f))
                    * densityProvider.density
        )
    }

    // the first part is responsible for scale while the gesture
    // is being performed

    // the second part of the equation is responsible for scale
    // animation that happens after finish() is called
    private val processedScale = {
        1f - ((gestureData.progress * 0.1f)
                + ((1f - progressAnimatable.value) * 0.15f))
    }

    override val exitModifier: Modifier
        get() = Modifier.exitModifier(
            processedOffset,
            processedScale,
            gestureData.progress,
            progressAnimatable.value
        )

    override val enterModifier: Modifier get() = Modifier.enterModifier(progressAnimatable.value)

    override fun onStart(event: BackEvent) {
        gestureOffsetWhenStarted = Offset(event.touchX, event.touchY)
    }

    override fun animate(event: BackEvent) {
        gestureData = event
    }

    override suspend fun finish() {
        progressAnimatable.animateTo(targetValue = 0F, animationSpec = tween(durationMillis = 300))
    }
}

private fun Modifier.exitModifier(
    processedOffset: () -> Offset,
    processedScale: () -> Float,
    gestureProgress: Float,
    windowTransitionProgress: Float
): Modifier = graphicsLayer {
    scaleX = processedScale()
    scaleY = processedScale()

    translationX = processedOffset().x
    translationY = processedOffset().y

    clip = true
    shape = RoundedCornerShape(((32 * gestureProgress) * windowTransitionProgress).dp)

    alpha = windowTransitionProgress
}

private fun Modifier.enterModifier(windowTransitionProgress: Float): Modifier = drawWithContent {
    drawContent()
    drawRect(color = Color(0F, 0F, 0F, alpha = 0.2f * windowTransitionProgress))
}






class PredictiveBackAnimation<C : Any, T : Any>(
    private val backHandler: BackHandler,
    private val animation: StackAnimation<C, T>,
    private val selector: (BackEvent, exitChild: Child.Created<C, T>, enterChild: Child.Created<C, T>) -> PredictiveBackAnimatable,
    private val onBack: () -> Unit,
) : StackAnimation<C, T> {

    @Composable
    override fun invoke(stack: ChildStack<C, T>, modifier: Modifier, content: @Composable (child: Child.Created<C, T>) -> Unit) {
        var activeConfigurations: Set<C> by remember { mutableStateOf(emptySet()) }

        val childContent =
            remember(content) {
                movableContentOf<Child.Created<C, T>> { child ->
                    key(child.configuration) {
                        content(child)

                        DisposableEffect(Unit) {
                            activeConfigurations += child.configuration
                            onDispose { activeConfigurations -= child.configuration }
                        }
                    }
                }
            }

        var data: Data<C, T> by rememberMutableStateWithLatest(key = stack) { latestData ->
            Data(stack = stack, key = latestData?.nextKey ?: 0)
        }

        val (dataStack, dataKey, dataAnimatable) = data

        val items =
            if (dataAnimatable == null) {
                listOf(Item(stack = dataStack, key = dataKey, modifier = Modifier))
            } else {
                listOf(
                    Item(stack = dataStack.dropLast(), key = dataKey + 1, modifier = dataAnimatable.enterModifier),
                    Item(stack = dataStack, key = dataKey, modifier = dataAnimatable.exitModifier),
                )
            }

        Box(modifier = modifier) {
            items.forEach { item ->
                key(item.key) {
                    animation(
                        stack = item.stack,
                        modifier = Modifier.fillMaxSize().then(item.modifier),
                        content = childContent,
                    )
                }
            }
        }

        val isBackEnabled = dataStack.backStack.isNotEmpty()
        val isBackGestureEnabled = isBackEnabled && ((dataAnimatable != null) || (activeConfigurations.size == 1))

        if (isBackEnabled) {
            if (isBackGestureEnabled) {
                val scope = rememberCoroutineScope()

                BackGestureHandler(
                    backHandler = backHandler,
                    onBackStarted = {
                        data = data.copy(animatable = selector(it, data.stack.active, data.stack.backStack.last()))
                        data.animatable?.onStart(it)
                    },
                    onBackProgressed = {
                        data.animatable?.animate(it)
                    },
                    onBackCancelled = {
                        data = data.copy(animatable = null)
                    },
                    onBack = {
                        if (data.animatable == null) {
                            onBack()
                        } else {
                            scope.launch {
                                data.animatable?.finish()
                                onBack()
                            }
                        }
                    }
                )
            } else {
                BackGestureHandler(backHandler = backHandler, onBack = onBack)
            }
        }
    }

    @Composable
    private fun <T : Any> rememberMutableStateWithLatest(
        key: Any,
        getValue: (latestValue: T?) -> T,
    ): MutableState<T> {
        val latestValue: Holder<T?> = remember { Holder(value = null) }
        val state = remember(key) { mutableStateOf(getValue(latestValue.value)) }
        latestValue.value = state.value

        return state
    }

    private fun <C : Any, T : Any> ChildStack<C, T>.dropLast(): ChildStack<C, T> =
        ChildStack(active = backStack.last(), backStack = backStack.dropLast(1))

    private data class Data<out C : Any, out T : Any>(
        val stack: ChildStack<C, T>,
        val key: Int,
        val animatable: PredictiveBackAnimatable? = null,
    ) {
        val nextKey: Int get() = if (animatable == null) key else key + 1
    }

    private data class Item<out C : Any, out T : Any>(
        val stack: ChildStack<C, T>,
        val key: Int,
        val modifier: Modifier,
    )

    private class Holder<T>(var value: T)
}

@Composable
fun BackGestureHandler(
    backHandler: BackHandler,
    onBackStarted: (BackEvent) -> Unit = {},
    onBackProgressed: (BackEvent) -> Unit = {},
    onBackCancelled: () -> Unit = {},
    onBack: () -> Unit,
) {
    val callback =
        BackCallback(
            onBackStarted = onBackStarted,
            onBackProgressed = onBackProgressed,
            onBackCancelled = onBackCancelled,
            onBack = onBack,
        )

    DisposableEffect(backHandler) {
        backHandler.register(callback)
        onDispose { backHandler.unregister(callback) }
    }
}

@ExperimentalDecomposeApi
interface PredictiveBackAnimatable {

    /**
     * Returns a [Modifier] for the child being removed (the currently active child).
     * The property must be Compose-observable, e.g. be backed by a Compose state.
     */
    val exitModifier: Modifier

    /**
     * Returns a [Modifier] for the child being shown (the previous child, behind the currently active child).
     * The property must be Compose-observable, e.g. be backed by a Compose state.
     */
    val enterModifier: Modifier

    fun onStart(event: BackEvent)

    /**
     * Animates both [exitModifier] and [enterModifier] according to [event].
     * Any previous animation must be cancelled.
     *
     * @see androidx.compose.animation.core.Animatable
     */
    fun animate(event: BackEvent)

    /**
     * Animates both [exitModifier] and [enterModifier] towards the final state.
     * Any previous animation must be cancelled.
     *
     * @see androidx.compose.animation.core.Animatable
     */
    suspend fun finish()
}
