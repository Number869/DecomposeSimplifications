package com.number869.decomposeSimplifications.core.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

inline fun <reified T: DecomposeViewModel> ComponentContext.decomposeViewModel(
    viewModel: T,
    key: Any? = null
) = if (key == null) {
    this.instanceKeeper.getOrCreate { viewModel }
} else {
    this.instanceKeeper.getOrCreate(key) { viewModel }
}

@Composable
inline fun <reified T: DecomposeViewModel> decomposeViewModel(
    viewModel: T,
    key: Any? = null,
    componentContext: ComponentContext = checkNotNull(LocalDecomposeComponentContext.current) {
        "No ComponentContext was provided via LocalDecomposeComponentContext"
    }
) = remember {
    if (key == null) {
        componentContext.instanceKeeper.getOrCreate { viewModel }
    } else {
        componentContext.instanceKeeper.getOrCreate(key) { viewModel }
    }
}

/**
 * Basic view model that is similar to the one that's offered by google for android
 */
open class DecomposeViewModel : InstanceKeeper.Instance {

    val viewModelScope: CoroutineScope
        get() = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
}