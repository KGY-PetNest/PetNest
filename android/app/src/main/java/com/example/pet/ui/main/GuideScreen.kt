package com.example.pet.ui.main

import com.example.pet.ui.components.ScreenHorizontalPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.pet.R
import com.example.pet.ui.components.ScreenHeader

@Composable
fun GuideScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ScreenHorizontalPadding)
    ) {
        ScreenHeader(title = stringResource(R.string.text_11_1), onBack = onBack)
    }
}