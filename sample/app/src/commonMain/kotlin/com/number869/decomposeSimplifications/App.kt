package com.number869.decomposeSimplifications

import DecomposeChildInstance
import DecomposeNavController
import DecomposeNavHost
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.*
import kotlinx.serialization.Serializable
import rememberDecomposeNavController
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    navController: DecomposeNavController<Screen> = rememberDecomposeNavController(
        startingDestination = Screen.Category1.Default,
        serializer = Screen.serializer()
    ),
    animation: StackAnimation<Screen, DecomposeChildInstance<Screen>> = stackAnimation(fade() + scale())
) = MaterialTheme {
    val currentScreen = navController.currentDestination

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Decompose Simplifications") })
        },
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
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
    ) { scaffoldPadding ->
        DecomposeNavHost(
            navController,
            Modifier.padding(scaffoldPadding),
            animation = animation
        ) { destination, _, _ ->
            when (destination) {
                Screen.Category1.Default -> {
                    Box(
                        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column {
                            Text("Category 1 Default")

                            Button(
                                onClick = {
                                    val random = UUID.randomUUID().toString()
                                    navController.navigate(Screen.Category1.Option1(id = random))
                                }
                            ) {
                                Text("Navigate to Option 1")
                            }
                        }
                    }
                }

                // because it's a data class and not a data object
                is Screen.Category1.Option1 -> {
                    Box(
                        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Category 1 Option 1 ${destination.id}")
                    }
                }

                Screen.Category2.Default -> {
                    Box(
                        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column {
                            Text("Category 2 Default")

                            Button(
                                onClick = {
                                    val random = UUID.randomUUID().toString()
                                    navController.navigate(Screen.Category2.Option1(id = random))
                                }
                            ) {
                                Text("Navigate to Option 1")
                            }
                        }
                    }
                }

                // because it's a data class and not a data object
                is Screen.Category2.Option1 -> {
                    Box(
                        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Category 2 Option 1 ${destination.id}")
                    }
                }
            }
        }
    }
}

@Serializable
sealed interface Screen {
    @Serializable
    sealed interface Category1 : Screen {
        @Serializable
        data object Default : Category1

        @Serializable
        data class Option1(val id: String) : Category1
    }

    @Serializable
    sealed interface Category2 : Screen {
        @Serializable
        data object Default : Category2

        @Serializable
        data class Option1(val id: String) : Category2
    }
}