@file:OptIn(ExperimentalDecomposeApi::class)

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.RoundedCorner
import android.view.WindowInsets
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.*
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.predictiveback.PredictiveBackAnimatable
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.essenty.backhandler.BackEvent
import com.arkivanov.essenty.backhandler.BackHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun <C : Any, T : Any> materialPredictiveBackAnimation(
    backHandler: BackHandler,
    animation: StackAnimation<C, T> = stackAnimation(fade() + scale()),
    onBack: () -> Unit,
    windowInsets: WindowInsets? = null,
    selector: (
        initialBackEvent: BackEvent,
        exitChild: Child.Created<C, T>,
        enterChild: Child.Created<C, T>
    ) -> PredictiveBackAnimatable = { initialBackEvent, exitChild, enterChild ->
        MaterialPredictiveBackAnimatable(initialBackEvent, windowInsets)
    },
): StackAnimation<C, T> {
    return predictiveBackAnimation(backHandler, animation, selector, onBack)
}

@OptIn(ExperimentalDecomposeApi::class)
private class MaterialPredictiveBackAnimatable(
    private val initialEvent: BackEvent,
    val windowInsets: WindowInsets?
) : PredictiveBackAnimatable {
    private val finishProgressAnimatable = Animatable(initialValue = 1F)
    private val finishProgress by derivedStateOf { finishProgressAnimatable.value }
    private val progressAnimatable = Animatable(initialValue = initialEvent.progress)
    private val progress by derivedStateOf { progressAnimatable.value }
    private var edge by mutableStateOf(initialEvent.swipeEdge)
    private var touchY by mutableFloatStateOf(initialEvent.touchY)

    override val exitModifier: Modifier
        @RequiresApi(VERSION_CODES.S)
        get() =
            Modifier
                .scaleFromEdge()
                .alpha(finishProgress)

    override val enterModifier: Modifier
        get() =
            Modifier.drawWithContent {
                drawContent()
                drawRect(color = Color(red = 0F, green = 0F, blue = 0F, alpha = finishProgress * 0.20F))
            }

    private fun Modifier.scaleFromEdge(): Modifier =
        graphicsLayer {
            // is 0 when window insets are null or the api level is too low
            val deviceRoundedCorner = if (VERSION.SDK_INT >= VERSION_CODES.S) {
                windowInsets?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)?.radius
            } else null

            // if deviceRoundedCorner is null - fall back to the old animation
            val roundedCorners = (deviceRoundedCorner?.div(density))?.dp ?: (16 * progress).dp

            clip = true
            shape = RoundedCornerShape(roundedCorners)

            val pivotFractionX =
                when (edge) {
                    BackEvent.SwipeEdge.LEFT -> 1F
                    BackEvent.SwipeEdge.RIGHT -> 0F
                    BackEvent.SwipeEdge.UNKNOWN -> 0.5F
                }

            transformOrigin = TransformOrigin(pivotFractionX = pivotFractionX, pivotFractionY = 0.5F)

            val scale = 1F - progress * 0.1F
            scaleX = scale
            scaleY = scale

            val translationXLimit =
                when (edge) {
                    BackEvent.SwipeEdge.LEFT -> -8.dp.toPx()
                    BackEvent.SwipeEdge.RIGHT -> 8.dp.toPx()
                    BackEvent.SwipeEdge.UNKNOWN -> 0F
                }

            translationX = translationXLimit * progress

            val translationYLimit = size.height / 20F - 8.dp.toPx()
            translationY = (translationYLimit * ((touchY - initialEvent.touchY) / size.height))  * (progress * 3).coerceAtMost(1f)
        }

    override suspend fun animate(event: BackEvent) {
        edge = event.swipeEdge
        touchY = event.touchY
        progressAnimatable.animateTo(event.progress)
    }

    override suspend fun finish() {
        val velocityFactor = progressAnimatable.velocity.coerceAtMost(1F) / 1F
        val progress = progressAnimatable.value
        coroutineScope {
            launch { progressAnimatable.animateTo(progress + (1F - progress) * velocityFactor) }
            launch { finishProgressAnimatable.animateTo(targetValue = 0F, animationSpec = tween()) }
        }
    }
}