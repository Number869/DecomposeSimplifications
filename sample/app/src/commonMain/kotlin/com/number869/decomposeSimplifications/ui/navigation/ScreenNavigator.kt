package com.number869.decomposeSimplifications.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.number869.decomposeSimplifications.core.common.DecomposeNavController
import com.number869.decomposeSimplifications.core.common.DecomposeNavHost
import com.number869.decomposeSimplifications.ui.screens.category1Default.Category1DefaultScreen
import com.number869.decomposeSimplifications.ui.screens.category1Option1.Category1Option1Screen
import com.number869.decomposeSimplifications.ui.screens.category2default.Category2DefaultScreen
import com.number869.decomposeSimplifications.ui.screens.category2option1.Category2Option1Screen
import org.koin.compose.getKoin

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun ScreenNavigator(modifier: Modifier = Modifier, ) {
    val navController = getKoin().get<DecomposeNavController<Screen>>()

    DecomposeNavHost(
        navController,
        modifier,
        animation = predictiveBackAnimation(
            backHandler = navController::backHandler.get(),
            animation = stackAnimation(fade() + scale()),
            onBack = navController::pop
        )
    ) { destination, componentContext, instance ->
        when (destination) {
            is Screen.Category1.Default -> Category1DefaultScreen(navController)

            is Screen.Category1.Option1 -> Category1Option1Screen(id = destination.id)

            is Screen.Category2.Default -> Category2DefaultScreen(navController)

            is Screen.Category2.Option1 -> Category2Option1Screen(id = destination.id)
        }
    }
}