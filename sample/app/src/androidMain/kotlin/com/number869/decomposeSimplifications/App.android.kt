package com.number869.decomposeSimplifications

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.defaultComponentContext
import com.number869.decomposeSimplifications.core.common.decomposeNavControllerFlex
import org.koin.core.context.GlobalContext
import org.koin.dsl.module

class AppActivity : ComponentActivity() {
    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalContext.loadKoinModules(
            module {
                single { decomposeNavControllerFlex(defaultComponentContext()) }
            }
        )

        setContent {
            enableEdgeToEdge()

            App()
        }
    }
}