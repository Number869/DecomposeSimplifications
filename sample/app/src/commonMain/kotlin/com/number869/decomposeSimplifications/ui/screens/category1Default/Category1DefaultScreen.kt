package com.number869.decomposeSimplifications.ui.screens.category1Default

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.number869.decomposeSimplifications.core.common.DecomposeNavControllerFlex
import com.number869.decomposeSimplifications.core.common.decomposeViewModel
import com.number869.decomposeSimplifications.ui.navigation.Screens
import com.number869.decomposeSimplifications.ui.screens.category1Option1.Category1Option1Screen
import java.util.*

@Composable
fun Category1DefaultScreen(navController: DecomposeNavControllerFlex, ) {
    val vm = decomposeViewModel(Category1DefaultViewModel())
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text("Category 1 Default")
            Text(vm.randomUuid)

            Button(
                onClick = {
                    val randomId = UUID.randomUUID().toString()
                    navController.openInOverlay(
                        "${Screens.Category1.Option1}$randomId"
                    ) { Category1Option1Screen(randomId, navController) }
                }
            ) {
                Text("Navigate to Option 1")
            }

            Button(
                onClick = {
                    navController.openInOverlay("alert dialog hi") {
                        HiAlertDialog(navController)
                    }
                }
            ) {
                Text("Open a dialog")
            }
        }
    }
}

@Composable
fun HiAlertDialog(navController: DecomposeNavControllerFlex) = AlertDialog(
    onDismissRequest = navController::navigateBack,
    text = { Text("hiiiiiii") },
    confirmButton = {
        FilledTonalButton(
            onClick = {
                navController.openAsScreen(Screens.Category1.Option1.destinationName) {
                    Category1Option1Screen("from dialog :3", navController)
                }
            }
        ) {
            Text("Open option 1 screen")
        }
        Button(onClick = navController::navigateBack) {
            Text("Close")
        }
    }
)