package com.number869.decomposeSimplifications.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    sealed interface Category1 : Screen {
        @Serializable
        data object Default : Category1

        @Serializable
        data class Option1(val id: String) : Category1
    }

    @Serializable
    sealed interface Category2 : Screen {
        @Serializable
        data object Default : Category2

        @Serializable
        data class Option1(val id: String) : Category2
    }
}