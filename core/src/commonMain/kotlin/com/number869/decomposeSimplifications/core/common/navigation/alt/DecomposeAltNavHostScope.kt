package com.number869.decomposeSimplifications.core.common.navigation.alt

import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import kotlin.reflect.KClass

class DecomposeAltNavHostScope<T: Any>() {
    val animationsHolder = mutableMapOf<KClass<*>, StackAnimator?>()

    @Composable
    inline fun <reified D: Any> screen(
        animation: StackAnimator? = defaultScreenAnimation,
        crossinline content: @Composable (D) -> Unit
    ) {
        if (LocalContentType.current == ContentTypes.Screen) {
            animationsHolder[D::class] = animation

            if (LocalDecomposeConfiguration.current is D) {
                content(LocalDecomposeConfiguration.current as D)
            }
        }
    }

    @Composable
    inline fun <reified D> overlay(
        animation: StackAnimator? = defaultScreenAnimation,
//        enablePredictiveBack: Boolean = false,
        crossinline content: @Composable (D) -> Unit
    ) {
        if (LocalContentType.current == ContentTypes.Overlay) {
            animationsHolder[D::class] = animation

            val destination = LocalDecomposeConfiguration.current

            if (destination is D) {
//                LocalDecomposeComponentContext.current.backHandler.register(
//                     BackCallback(!enablePredictiveBack) { navController.closeOverlay(destination) }
//                )
                content(LocalDecomposeConfiguration.current as D)
            }
        }
    }

    val LocalDecomposeConfiguration = compositionLocalOf<T> {
        // Provide a default value for the composition local if needed
        error("No decompose configuration provided")
    }
}


val LocalContentType = compositionLocalOf<ContentTypes> {
    // Provide a default value for the composition local if needed
    error("No ContentType provided")
}
enum class ContentTypes { Screen, Overlay }

val defaultScreenAnimation = fade(tween(200)) + scale(tween(200))