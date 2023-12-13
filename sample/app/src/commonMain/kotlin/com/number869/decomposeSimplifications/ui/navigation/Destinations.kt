package com.number869.decomposeSimplifications.ui.navigation

sealed interface Screens {
    sealed class Category1() {
        val destinationName = "$categoryName$this"
        data object Default : Category1()
        data object Option1 : Category1()
        companion object {
            /**
             * This is only used for tracking for selected parameters in navigation items in the
             * navigation bar
             */
            const val categoryName: String = "Category1"
        }
    }

    sealed class Category2() {
        // category name + destination name, i.e. "Category2Default"
        val destinationName = "$categoryName$this"
        data object Default : Category2()
        data object Option1 : Category2()

        companion object {
            const val categoryName: String = "Category2"
        }
    }
}