package com.number869.decomposeSimplifications.ui.screens.category2option1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.number869.decomposeSimplifications.core.common.navigation.alt.DecomposeAltNavController
import com.number869.decomposeSimplifications.ui.navigation.Destinations

@Composable
fun Category2Option1Screen(id: String, navController: DecomposeAltNavController<Destinations>) {
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Text("Category 2 Option 1 $id")
    }
}