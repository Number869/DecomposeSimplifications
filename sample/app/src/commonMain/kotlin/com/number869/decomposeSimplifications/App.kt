package com.number869.decomposeSimplifications

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.number869.decomposeSimplifications.common.BasicBottomSheet
import com.number869.decomposeSimplifications.core.common.navigation.alt.DecomposeAltNavController
import com.number869.decomposeSimplifications.core.common.navigation.alt.DecomposeAltNavHost
import com.number869.decomposeSimplifications.ui.navigation.Destinations
import com.number869.decomposeSimplifications.ui.screens.category1Default.Category1DefaultScreen
import com.number869.decomposeSimplifications.ui.screens.category1Default.HiAlertDialog
import com.number869.decomposeSimplifications.ui.screens.category1Option1.Category1Option1Screen
import com.number869.decomposeSimplifications.ui.screens.category1Option1.MeowBottomsheetContent
import com.number869.decomposeSimplifications.ui.screens.category1Option1.MeowDialog
import com.number869.decomposeSimplifications.ui.screens.category2default.Category2DefaultScreen
import com.number869.decomposeSimplifications.ui.screens.category2option1.Category2Option1Screen
import com.number869.decomposeSimplifications.ui.theme.SampleTheme
import org.koin.compose.getKoin


@OptIn(ExperimentalMaterial3Api::class, ExperimentalDecomposeApi::class)
@Composable
fun App() {
    val navController = getKoin().get<DecomposeAltNavController<Destinations>>()

    SampleTheme {
        Surface {
            DecomposeAltNavHost(
                navController,
                startingDestination = Destinations.Category1.Default,
                screenContainer = { containerContent ->
                    Scaffold(bottomBar = { SampleNavBar(navController) }) { scaffoldPadding ->
                        containerContent(Modifier.padding(scaffoldPadding))
                    }
                }
            ) {
                screen<Destinations.Category1.Default> {
                    Category1DefaultScreen(navController)
                }
                screen<Destinations.Category1.Option1> {
                    Category1Option1Screen(it.id, navController)
                }

                screen<Destinations.Category2.Default>(animation = fade(tween(200))) {
                    Category2DefaultScreen(navController)
                }
                screen<Destinations.Category2.Option1> {
                    Category2Option1Screen(it.id, navController)
                }

                overlay<Destinations.Category1.Option1> {
                    Category1Option1Screen(it.id, navController)
                }

                overlay<Destinations.Category2.Default>(animation = slide()) {
                    Category2DefaultScreen(navController)
                }

                overlay<Destinations.Overlay.ExampleBottomSheet>(null) {
                    BasicBottomSheet(onDismiss = navController::navigateBack) {
                        MeowBottomsheetContent(navController)
                    }
                }

                overlay<Destinations.Overlay.Category1Dialog>(null) { HiAlertDialog(navController) }

                overlay<Destinations.Overlay.DialogFromBottomSheet>(null) { MeowDialog(navController) }
            }
        }
    }
}

@Composable
fun SampleNavBar(navController: DecomposeAltNavController<Destinations>) {
    val currentScreen = navController.currentScreenDestination

    NavigationBar {
        NavigationBarItem(
            selected = currentScreen is Destinations.Category1,
            icon = { Icon(Icons.Default.Home, contentDescription = null)},
            onClick = { navController.navigateToScreen(Destinations.Category1.Default)}
        )

        NavigationBarItem(
            selected = currentScreen is Destinations.Category2,
            icon = { Icon(Icons.Default.Home, contentDescription = null)},
            onClick = { navController.navigateToScreen(Destinations.Category2.Default) }
        )
    }
}

