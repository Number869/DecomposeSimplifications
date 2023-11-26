package com.number869.decomposeSimplifications.ui.screens.category1Default

import com.number869.decomposeSimplifications.core.common.DecomposeViewModel
import kotlinx.coroutines.launch
import java.util.*

class Category1DefaultViewModel() : DecomposeViewModel() {
    var randomUuid = "randomUuid in the view model is empty"

    init {
        viewModelScope.launch {
            randomUuid = UUID.randomUUID().toString()
        }
    }
}