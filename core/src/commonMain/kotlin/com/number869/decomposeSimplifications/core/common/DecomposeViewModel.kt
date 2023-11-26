package com.number869.decomposeSimplifications.core.common

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

inline fun <reified T: DecomposeViewModel> ComponentContext.decomposeViewModel(viewModel: T) = this.instanceKeeper.getOrCreate { viewModel }

/**
 * Basic view model that is similar to the one that's offered by google for android
 */
open class DecomposeViewModel : InstanceKeeper.Instance {
    val viewModelScope: CoroutineScope
        get() = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
}