package com.number869.decomposeSimplifications.ui.screens.category2default

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
import com.number869.decomposeSimplifications.core.common.navigation.alt.DecomposeAltNavController
import com.number869.decomposeSimplifications.ui.navigation.Destinations
import java.util.*

@Composable
fun Category2DefaultScreen(navController: DecomposeAltNavController<Destinations>) {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text("Category 2 Default")

            Button(
                onClick = {
                    val randomId = UUID.randomUUID().toString()
                    navController.navigateToScreen(Destinations.Category2.Option1(randomId))
                }
            ) {
                Text("Navigate to Option 1")
            }
        }
    }
}