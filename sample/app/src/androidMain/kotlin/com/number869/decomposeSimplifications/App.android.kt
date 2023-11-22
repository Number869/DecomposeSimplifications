package com.number869.decomposeSimplifications

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.defaultComponentContext
import materialPredictiveBackAnimation
import rememberDecomposeNavController

class AppActivity : ComponentActivity() {
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberDecomposeNavController(
                startingDestination = Screen.Category1.Default,
                defaultComponentContext(),
                serializer = Screen.serializer()
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