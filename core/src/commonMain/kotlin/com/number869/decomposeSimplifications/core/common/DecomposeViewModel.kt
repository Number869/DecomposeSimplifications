package com.number869.decomposeSimplifications.core.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


/**
 * Android-like view model instancer. Will get or create a view model instance in the [InstanceKeeper]
 * from Essenty. Provide [key] if you have multiple instances of the same view model
 * else it will get the first created one.
 *
 * Note: this is not composable therefore this is not remembered, so wrap this into
 * remember if you're using this inside a composable function to avoid [getOrCreate]
 * being called on each recomposition of the parent.
 */
inline fun <reified T: DecomposeViewModel> ComponentContext.decomposeViewModel(
    viewModel: T,
    key: Any? = null
) = if (key == null) {
    this.instanceKeeper.getOrCreate { viewModel }
} else {
    this.instanceKeeper.getOrCreate(key) { viewModel }
}

/**
 * Android-like view model instancer. Will get or create a view model instance in the [InstanceKeeper]
 * from Essenty. Provide [key] if you have multiple instances of the same view model
 * else it will get the first created one.
 */
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
 * Basic view model that is similar to the one that's offered by google for android.
 */
open class DecomposeViewModel : InstanceKeeper.Instance {

    val viewModelScope: CoroutineScope
        get() = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
}