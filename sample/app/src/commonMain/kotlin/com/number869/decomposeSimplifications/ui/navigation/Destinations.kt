package com.number869.decomposeSimplifications.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destinations {
    @Serializable
    data object Empty : Destinations
    @Serializable
    sealed interface Category1 : Destinations {
        @Serializable
        data object Default : Category1

        @Serializable
        data class Option1(val id: String) : Category1
    }

    @Serializable
    sealed interface Category2 : Destinations {
        @Serializable
        data object Default : Category2

        @Serializable
        data class Option1(val id: String) : Category2
    }

    @Serializable
    sealed interface Overlay : Destinations {
        @Serializable
        data object ExampleBottomSheet : Overlay
        data object Category1Dialog : Overlay
        data object DialogFromBottomSheet : Overlay
    }
}