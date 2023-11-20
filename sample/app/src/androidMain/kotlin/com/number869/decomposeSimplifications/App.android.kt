package com.number869.decomposeSimplifications

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.defaultComponentContext
import materialPredictiveBackAnimation
import rememberDecomposeNavController

class AppActivity : ComponentActivity() {
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberDecomposeNavController<Screen>(
                startingDestination = Screen.Category1.Default,
                defaultComponentContext(),
            )

            App(
                navController,
                animation = materialPredictiveBackAnimation(
                    backHandler = navController.backHandler,
                    windowWidthDp = LocalConfiguration.current.screenWidthDp,
                    onBack = navController::pop,
                    densityProvider = LocalDensity.current
                )
            )
        }
    }
}