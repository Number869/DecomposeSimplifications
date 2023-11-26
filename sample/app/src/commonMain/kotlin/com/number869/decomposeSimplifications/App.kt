package com.number869.decomposeSimplifications

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.*
import com.number869.decomposeSimplifications.core.common.DecomposeChildInstance
import com.number869.decomposeSimplifications.core.common.DecomposeNavController
import com.number869.decomposeSimplifications.core.common.rememberDecomposeNavController
import com.number869.decomposeSimplifications.ui.navigation.Screen
import com.number869.decomposeSimplifications.ui.navigation.ScreenNavigator


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    navController: DecomposeNavController<Screen> = rememberDecomposeNavController(
        startingDestination = Screen.Category1.Default,
        serializer = Screen.serializer()
    ),
    animation: StackAnimation<Screen, DecomposeChildInstance<Screen>> = stackAnimation(fade() + scale())
) {
    MaterialTheme {
        Scaffold(
            topBar = { CenterAlignedTopAppBar(title = { Text("Decompose Simplifications") }) },
            bottomBar = { SampleNavBar(navController) }
        ) { scaffoldPadding ->
            ScreenNavigator(
                Modifier.padding(scaffoldPadding),
                navController,
                animation
            )
        }
    }
}

@Composable
fun SampleNavBar(navController: DecomposeNavController<Screen>) {
    val currentScreen = navController.currentDestination

    NavigationBar {
        NavigationBarItem(
            selected = currentScreen is Screen.Category1,
            icon = { Icon(Icons.Default.Home, contentDescription = null)},
            onClick = { navController.navigate(Screen.Category1.Default) }
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Category2,
            icon = { Icon(Icons.Default.Home, contentDescription = null)},
            onClick = { navController.navigate(Screen.Category2.Default) }
        )
    }
}