package com.number869.decomposeSimplifications.core.common.ultils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier

fun Modifier.noRippleClickable(onClick: () -> Unit = {}) = this.clickable(
    indication = null,
    interactionSource = MutableInteractionSource(),
    onClick = onClick
)