package com.example.pet.ui.mappicker

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.pet.R
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.adaptiveContentWidth
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.SearchType
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.runtime.Error
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

private val START_POINT = Point(55.751244, 37.618423)
private const val START_ZOOM = 14f

@Composable
fun MapPickerScreen(
    onBack: () -> Unit,
    onPicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    var address by remember { mutableStateOf<String?>(null) }
    var failed by remember { mutableStateOf(false) }
    var session by remember { mutableStateOf<Session?>(null) }

    val searchManager = remember {
        SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    }
    val searchOptions = remember {
        SearchOptions().apply {
            searchTypes = SearchType.GEO.value
            resultPageSize = 1
        }
    }

    val searchListener = remember {
        object : Session.SearchListener {
            override fun onSearchResponse(response: Response) {
                val obj = response.collection.children.firstOrNull()?.obj
                val text = obj?.metadataContainer
                    ?.getItem(ToponymObjectMetadata::class.java)
                    ?.address?.formattedAddress
                    ?: obj?.name
                if (text != null) address = text else failed = true
            }

            override fun onSearchError(error: Error) {
                failed = true
            }
        }
    }

    val cameraListener = remember {
        CameraListener { _, position, _, finished ->
            if (finished) {
                session?.cancel()
                address = null
                failed = false
                session = searchManager.submit(
                    position.target,
                    position.zoom.roundToInt(),
                    searchOptions,
                    searchListener
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        session = searchManager.submit(START_POINT, START_ZOOM.roundToInt(), searchOptions, searchListener)
    }

    DisposableEffect(lifecycleOwner) {
        var started = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> if (!started) {
                    started = true
                    MapKitFactory.getInstance().onStart()
                    mapView.onStart()
                }
                Lifecycle.Event.ON_STOP -> if (started) {
                    started = false
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (started) {
                mapView.onStop()
                MapKitFactory.getInstance().onStop()
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .adaptiveContentWidth()
                .padding(horizontal = 16.dp)
        ) {
            ScreenHeader(title = stringResource(R.string.text_7_1), onBack = onBack)
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    factory = {
                        mapView.apply {
                            mapWindow.map.move(CameraPosition(START_POINT, START_ZOOM, 0f, 0f))
                            mapWindow.map.addCameraListener(WeakReference(cameraListener))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-24).dp)
                        .size(48.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = address ?: stringResource(
                    if (failed) R.string.text_7_3 else R.string.text_7_2
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = if (address != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            )

            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                text = stringResource(R.string.text_7_4),
                onClick = { address?.let(onPicked) }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}