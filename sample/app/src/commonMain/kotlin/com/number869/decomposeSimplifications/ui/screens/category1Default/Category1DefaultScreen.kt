package com.number869.decomposeSimplifications.ui.screens.category1Default

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.number869.decomposeSimplifications.core.common.DecomposeNavController
import com.number869.decomposeSimplifications.ui.navigation.Screen
import java.util.*

@Composable
fun Category1DefaultScreen(
    vm: Category1DefaultViewModel,
    navController: DecomposeNavController<Screen>,
) {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text("Category 1 Default")
            Text(vm.randomUuid)

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