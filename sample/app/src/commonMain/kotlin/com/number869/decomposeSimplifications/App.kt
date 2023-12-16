package com.number869.decomposeSimplifications

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.number869.decomposeSimplifications.core.common.DecomposeNavControllerFlex
import com.number869.decomposeSimplifications.core.common.DecomposeNavHostFlex
import com.number869.decomposeSimplifications.core.common.StartingDestination
import com.number869.decomposeSimplifications.ui.navigation.Screens
import com.number869.decomposeSimplifications.ui.screens.category1Default.Category1DefaultScreen
import com.number869.decomposeSimplifications.ui.screens.category2default.Category2DefaultScreen
import com.number869.decomposeSimplifications.ui.theme.SampleTheme
import org.koin.compose.getKoin


@OptIn(ExperimentalMaterial3Api::class, ExperimentalDecomposeApi::class)
@Composable
fun App() {
    val navController = getKoin().get<DecomposeNavControllerFlex>()

    SampleTheme {
        Surface {
            DecomposeNavHostFlex(
                navController,
                startingPoint = StartingDestination(Screens.Category1.Default.destinationName) {
                    Category1DefaultScreen(navController)
                }
            ) { nonOverlayContent ->
                Scaffold(
                    topBar = { CenterAlignedTopAppBar(title = { Text("Decompose Simplifications") }) },
                    bottomBar = { SampleNavBar(navController) }
                ) { scaffoldPadding ->
                    nonOverlayContent(modifier = Modifier.padding(scaffoldPadding))
                }
            }
        }

    }
}

@Composable
fun SampleNavBar(navController: DecomposeNavControllerFlex) {
    val currentScreen = navController.currentScreenDestination

    NavigationBar {
        NavigationBarItem(
            selected = currentScreen.startsWith(Screens.Category1.categoryName),
            icon = { Icon(Icons.Default.Home, contentDescription = null)},
            onClick = { navController.openAsScreen("Category1Default") { Category1DefaultScreen(navController) } }
        )

        NavigationBarItem(
            selected = currentScreen.startsWith(Screens.Category2.categoryName),
            icon = { Icon(Icons.Default.Home, contentDescription = null)},
            onClick = { navController.openAsScreen("Category2Default") { Category2DefaultScreen(navController) } }
        )
    }
}

