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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.*
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackHandler
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds

fun <C : Any, T : Any> materialPredictiveBackAnimation(
    backHandler: BackHandler,
    animation: StackAnimation<C, T>? = stackAnimation(fade() + scale()),
    exitModifier: (processedOffset: Offset, processedScale: Float, gestureProgress: Float, windowTransitionProgress: Float) -> Modifier = { processedOffset, processedScale, gestureProgress, windowTransitionProgress ->
        Modifier.exitModifier(
            processedOffset,
            processedScale,
            gestureProgress,
            windowTransitionProgress
        )
    },
    enterModifier: (windowTransitionProgress: Float) -> Modifier = { Modifier.enterModifier(it) },
    onBack: () -> Unit,
    windowSize: IntSize,
    density: Density,
): StackAnimation<C, T> =  PredictiveBackAnimation(
    backHandler = backHandler,
    animation = animation ?: StackAnimation { stack, modifier, childContent ->
        Box(modifier = modifier) {
            childContent(stack.active)
        }
    },
    exitModifier = exitModifier,
    enterModifier = enterModifier,
    onBack = onBack,
    windowSize,
    density
)

private fun Modifier.exitModifier(
    processedOffset: Offset,
    processedScale: Float,
    gestureProgress: Float,
    windowTransitionProgress: Float
): Modifier = graphicsLayer {
    scaleX = processedScale
    scaleY = processedScale

    translationX = processedOffset.x
    translationY = processedOffset.y

    clip = true
    shape = RoundedCornerShape(((32 * gestureProgress) * windowTransitionProgress).dp)

    alpha = windowTransitionProgress
}

private fun Modifier.enterModifier(windowTransitionProgress: Float): Modifier =
    drawWithContent {
        drawContent()
        drawRect(color = Color(0F, 0F, 0F, alpha = 0.3f * windowTransitionProgress))
    }

public class PredictiveBackAnimation<C : Any, T : Any>(
    private val backHandler: BackHandler,
    private val animation: StackAnimation<C, T>,
    private val exitModifier: (
        processedOffset: Offset,
        processedScale: Float,
        gestureProgress: Float,
        windowTransitionProgress: Float
    ) -> Modifier,
    private val enterModifier: (windowTransitionProgress: Float) -> Modifier,
    private val onBack: () -> Unit,
    private val windowSize: IntSize,
    private val density: Density
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

        val currentKey = remember { Holder(value = 0) }

        var items: List<Item<C, T>> by rememberMutableStateWithLatest(
            key = stack,
            onReplaced = { latestItems -> currentKey.value = latestItems.maxOf(Item<*, *>::key) },
            getValue = { listOf(Item(stack = stack, key = currentKey.value)) },
        )

        Box(modifier = modifier) {
            items.forEach { item ->
                key(item.key) {
                    animation(
                        stack = item.stack,
                        modifier = Modifier
                            .fillMaxSize()
                            .then(item.modifier),
                        content = childContent,
                    )
                }
            }
        }

        val isBackEnabled = stack.backStack.isNotEmpty()
        val isBackGestureEnabled = isBackEnabled && ((items.size > 1) || (items.size == 1) && (activeConfigurations.size == 1))

        DisposableEffect(stack, isBackEnabled, isBackGestureEnabled) {
            if (!isBackEnabled) {
                return@DisposableEffect onDispose {}
            }

            val scope = CoroutineScope(Dispatchers.Main.immediate)

            val callback =
                if (isBackGestureEnabled) {
                    GestureBackCallback(
                        scope = scope,
                        stack = stack,
                        currentKey = currentKey.value,
                        exitModifier = exitModifier,
                        enterModifier = enterModifier,
                        setItems = { items = it },
                        onFinished = { newKey ->
                            currentKey.value = newKey
                            onBack()
                        },
                        windowSize,
                        density
                    )
                } else {
                    BackCallback(onBack = onBack)
                }

            backHandler.register(callback)

            onDispose {
                scope.cancel()
                backHandler.unregister(callback)
            }
        }
    }

    @Composable
    private fun <T : Any> rememberMutableStateWithLatest(
        key: Any,
        onReplaced: (latestValue: T) -> Unit,
        getValue: () -> T,
    ): MutableState<T> {
        val latestValue: Holder<T?> = remember { Holder(value = null) }

        val state =
            remember(key) {
                latestValue.value?.also(onReplaced)
                mutableStateOf(getValue())
            }

        latestValue.value = state.value

        return state
    }

    private data class StackItemData<out C : Any, out T : Any>(
        val exitItem: Item<C, T>,
        val enterItem: Item<C, T>,
    )

    private data class Item<out C : Any, out T : Any>(
        val stack: ChildStack<C, T>,
        val key: Int,
        val modifier: Modifier = Modifier,
    )

    private class Holder<T>(var value: T)

    private class GestureBackCallback<C : Any, T : Any>(
        private val scope: CoroutineScope,
        stack: ChildStack<C, T>,
        currentKey: Int,
        private val exitModifier: (
            processedOffset: Offset,
            processedScale: Float,
            gestureProgress: Float,
            windowTransitionProgress: Float
        ) -> Modifier,
        private val enterModifier: (windowTransitionProgress: Float) -> Modifier,
        private val setItems: (List<Item<C, T>>) -> Unit,
        private val onFinished: (newKey: Int) -> Unit,
        private val windowSize: IntSize,
        private val density: Density
    ) : BackCallback() {
        private fun ChildStack<C, T>.dropLast(): ChildStack<C, T> = ChildStack(
            active = backStack.last(),
            backStack = backStack.dropLast(1)
        )
        private var stackItemData: StackItemData<C, T> =
            StackItemData(
                exitItem = Item(stack = stack, key = currentKey),
                enterItem = Item(stack = stack.dropLast(), key = currentKey + 1),
            )

        var backEventData = BackEvent(0f, BackEvent.SwipeEdge.UNKNOWN, 0f, 0f)

        var mockAnimationProgress = 1f
        private var gestureOffsetWhenStarted = Offset.Zero

        override fun onBackStarted(backEvent: BackEvent) {
            updateBackEventData(backEvent)

            // remember where on the screen the gesture started
            gestureOffsetWhenStarted = Offset(backEvent.touchX, backEvent.touchY)
        }

        private fun updateBackEventData(backEvent: BackEvent) {
            backEventData = backEvent
            stackItemData = stackItemData.update()
            setItems(listOf(stackItemData.enterItem, stackItemData.exitItem))
        }

        override fun onBackProgressed(backEvent: BackEvent) {
            updateBackEventData(backEvent)
        }

        override fun onBackCancelled() {
            setItems(listOf(stackItemData.exitItem.copy(modifier = Modifier)))
        }

        override fun onBack() {
            scope.launch {
                while ((mockAnimationProgress > 0F) && isActive) {
                    delay(8.milliseconds) // for 120hz screens
                    mockAnimationProgress -= 0.2F
                    stackItemData = stackItemData.update(animationProgress = mockAnimationProgress)
                    setItems(listOf(stackItemData.enterItem, stackItemData.exitItem))
                }

                if (isActive) {
                    setItems(listOf(stackItemData.enterItem.copy(modifier = Modifier)))
                    onFinished(stackItemData.enterItem.key)
                }
            }
        }

        private fun StackItemData<C, T>.update(
            gestureProgress: Float = backEventData.progress,
            edge: BackEvent.SwipeEdge = backEventData.swipeEdge,
            animationProgress: Float = mockAnimationProgress
        ): StackItemData<C, T> {


            val gestureSwipeDistanceOffset = Offset(
                x = backEventData.touchX - gestureOffsetWhenStarted.x,
                y = backEventData.touchY - gestureOffsetWhenStarted.y
            )
            // when mockAnimationProgress is 0 - the offset will be also
            // 0, aka the default
            val processedOffset = Offset(
                x = if (edge == BackEvent.SwipeEdge.LEFT)
                    ((windowSize.width * 0.05f) - (8 * density.density)) * gestureProgress
                else
                    -(((windowSize.width * 0.05f) - (8 * density.density)) * gestureProgress),
                y = (gestureSwipeDistanceOffset.y * 0.05f) * density.density
            )

            // the first part is responsible for scale while the gesture
            // is being performed

            // the second part of the equation is responsible for scale
            // animation that happens after onBack() is called
            val processedScale = 1f - ((gestureProgress * 0.1f) + ((1f - animationProgress) * 0.05f))

            return copy(
                exitItem = exitItem.copy(modifier = exitModifier(processedOffset, processedScale, gestureProgress, animationProgress)),
                enterItem = enterItem.copy(modifier = enterModifier(animationProgress)),
            )
        }
    }
}