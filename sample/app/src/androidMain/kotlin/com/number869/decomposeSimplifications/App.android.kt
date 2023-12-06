package com.number869.decomposeSimplifications

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.defaultComponentContext
import com.number869.decomposeSimplifications.core.common.DecomposeNavController
import com.number869.decomposeSimplifications.core.common.decomposeNavController
import com.number869.decomposeSimplifications.ui.navigation.Screen
import org.koin.core.context.startKoin
import org.koin.dsl.module

class AppActivity : ComponentActivity() {
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            modules(
                module {
                    single<DecomposeNavController<Screen>> {
                        decomposeNavController(
                            startingDestination = Screen.Category1.Default,
                            defaultComponentContext(),
                            serializer = Screen.serializer()
                        )
                    }
                }
            )
        }
        setContent {
            enableEdgeToEdge()

            App()
        }
    }
}