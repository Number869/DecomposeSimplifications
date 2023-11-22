package com.number869.decomposeSimplifications

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.defaultComponentContext
import materialPredictiveBackAnimation
import rememberDecomposeNavController

class AppActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberDecomposeNavController<Screen>(
                startingDestination = Screen.Category1.Default,
                defaultComponentContext()
            )

            enableEdgeToEdge()

            App(
                navController,
                animation = materialPredictiveBackAnimation(
                    backHandler = navController.backHandler,
                    onBack = navController::pop,
                )
            )
        }
    }
}