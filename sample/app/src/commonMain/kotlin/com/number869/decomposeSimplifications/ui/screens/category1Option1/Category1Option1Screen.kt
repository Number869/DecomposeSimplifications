package com.number869.decomposeSimplifications.ui.screens.category1Option1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Category1Option1Screen(id: String, vm: Category1Option1ViewModel) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Category 1 Option 1 $id")
        Text("random uuid from vm that destroys itself after exit ${vm.randomUuid}")
    }
}