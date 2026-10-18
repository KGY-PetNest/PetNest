package com.example.pet.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pet.R

@Composable
fun PhotoViewerDialog(
    photoUri: String,
    title: String?,
    onDismiss: () -> Unit
) {
    val photo = rememberPhoto(photoUri)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding()
        ) {
            ScreenHeader(
                title = title.orEmpty(),
                onBack = onDismiss,
                modifier = Modifier.padding(horizontal = ScreenHorizontalPadding)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Crossfade(targetState = photo, label = "photoViewer") { bitmap ->
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (bitmap == null) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
                        } else {
                            ZoomableImage(
                                bitmap = bitmap,
                                contentDescription = stringResource(R.string.text_27_1)
                            )
                        }
                    }
                }
            }
        }
    }
}
