package com.number869.decomposeSimplifications.core.common.ultils

import androidx.compose.runtime.compositionLocalOf
import com.arkivanov.decompose.ComponentContext

val LocalDecomposeComponentContext = compositionLocalOf<ComponentContext> {
    // Provide a default value for the composition local if needed
    error("No ComponentContext provided")
}