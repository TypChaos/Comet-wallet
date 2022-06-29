package org.ergoplatform.desktop.ui.navigation

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.animation.child.slide

interface Component {
    @Composable
    fun render()

    @OptIn(ExperimentalDecomposeApi::class)
    val animation get() = slide()
}