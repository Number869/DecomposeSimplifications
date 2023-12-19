package com.number869.decomposeSimplifications.ui.screens.category1Default

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.number869.decomposeSimplifications.core.common.navigation.alt.DecomposeAltNavController
import com.number869.decomposeSimplifications.core.common.ultils.decomposeViewModel
import com.number869.decomposeSimplifications.ui.navigation.Destinations
import java.util.*

@Composable
fun Category1DefaultScreen(navController: DecomposeAltNavController<Destinations>, ) {
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

                    navController.navigateToOverlay(Destinations.Category1.Option1(randomId))
                }
            ) {
                Text("Navigate to Option 1")
            }

            Button(
                onClick = {
                    navController.navigateToOverlay(Destinations.Overlay.Category1Dialog)
                }
            ) {
                Text("Open a dialog")
            }
        }
    }
}

@Composable
fun HiAlertDialog(navController: DecomposeAltNavController<Destinations>) = AlertDialog(
    onDismissRequest = navController::navigateBack,
    text = { Text("hiiiiiii") },
    confirmButton = {
        FilledTonalButton(
            onClick = {
                navController.navigateToScreen(Destinations.Category1.Option1("from dialog :3"))
            }
        ) {
            Text("Open option 1 screen")
        }
        Button(onClick = navController::navigateBack) {
            Text("Close")
        }
    }
)