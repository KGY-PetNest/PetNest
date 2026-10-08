package com.example.pet.ui.mappicker

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.pet.R
import com.example.pet.data.GeoPoint
import com.example.pet.ui.components.FormRules
import com.example.pet.ui.components.LocationOutcome
import com.example.pet.ui.components.PrimaryButton
import com.example.pet.ui.components.ScreenHeader
import com.example.pet.ui.components.adaptiveContentWidth
import com.example.pet.ui.components.fetchLocationSilently
import com.example.pet.ui.components.rememberLocationRequester
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.VisibleRegionUtils
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
import kotlinx.coroutines.delay

private val START_POINT = Point(55.751244, 37.618423)
private const val START_ZOOM = 14f
private const val HOUSE_ZOOM = 16f
private const val ZOOM_IN_TARGET = 17f
private const val MY_LOCATION_ZOOM = 16f
private const val SEARCH_RESULT_ZOOM = 17f
private const val SUGGESTIONS_COUNT = 5
private const val MIN_QUERY_LENGTH = 3
private const val SEARCH_DEBOUNCE_MS = 400L

private data class AddressSuggestion(val title: String, val subtitle: String, val point: Point)

private val ADDRESS_BAR_HEIGHT = 56.dp

@Composable
fun MapPickerScreen(
    onBack: () -> Unit,
    onPicked: (String, GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
    forVolunteerLocation: Boolean = false,
    startPoint: GeoPoint? = null
) {
    val initialPoint = remember(startPoint) {
        startPoint?.let { Point(it.lat, it.lon) } ?: START_POINT
    }
    val initialZoom = if (startPoint != null) ZOOM_IN_TARGET else START_ZOOM
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    var address by remember { mutableStateOf<String?>(null) }
    var failed by remember { mutableStateOf(false) }
    var isMoving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var session by remember { mutableStateOf<Session?>(null) }
    var pickedPoint by remember { mutableStateOf(initialPoint) }
    var cameraZoom by remember { mutableStateOf(initialZoom) }
    val focusManager = LocalFocusManager.current
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<AddressSuggestion>>(emptyList()) }
    var noSuggestions by remember { mutableStateOf(false) }
    var searchFocused by remember { mutableStateOf(false) }
    var textSession by remember { mutableStateOf<Session?>(null) }

    val searchManager = remember {
        SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    }
    val searchOptions = remember {
        SearchOptions().apply {
            searchTypes = SearchType.GEO.value
            resultPageSize = 1
        }
    }

    val textSearchOptions = remember {
        SearchOptions().apply {
            searchTypes = SearchType.GEO.value
            resultPageSize = SUGGESTIONS_COUNT
        }
    }

    val textSearchListener = remember {
        object : Session.SearchListener {
            override fun onSearchResponse(response: Response) {
                suggestions = response.collection.children.mapNotNull { item ->
                    val obj = item.obj ?: return@mapNotNull null
                    val point = obj.geometry.firstOrNull()?.point ?: return@mapNotNull null
                    AddressSuggestion(
                        title = obj.name.orEmpty(),
                        subtitle = obj.descriptionText.orEmpty(),
                        point = point
                    )
                }
                noSuggestions = suggestions.isEmpty()
            }

            override fun onSearchError(error: Error) {
                suggestions = emptyList()
                noSuggestions = true
            }
        }
    }

    LaunchedEffect(query) {
        textSession?.cancel()
        val text = query.trim()
        if (text.length < MIN_QUERY_LENGTH) {
            suggestions = emptyList()
            noSuggestions = false
            return@LaunchedEffect
        }
        delay(SEARCH_DEBOUNCE_MS)
        val region = VisibleRegionUtils.toPolygon(mapView.mapWindow.map.visibleRegion)
        textSession = searchManager.submit(
            text,
            region,
            textSearchOptions,
            textSearchListener
        )
    }

    val searchListener = remember {
        object : Session.SearchListener {
            override fun onSearchResponse(response: Response) {
                val obj = response.collection.children.firstOrNull()?.obj
                val text = obj?.metadataContainer
                    ?.getItem(ToponymObjectMetadata::class.java)
                    ?.address?.formattedAddress
                    ?: obj?.name
                isLoading = false
                if (text != null) {
                    address = text
                    failed = false
                } else {
                    address = null
                    failed = true
                }
            }

            override fun onSearchError(error: Error) {
                isLoading = false
                address = null
                failed = true
            }
        }
    }

    val userMoved = remember { mutableStateOf(false) }
    val cameraListener = remember {
        CameraListener { _, position, reason, finished ->
            if (reason == CameraUpdateReason.GESTURES) userMoved.value = true
            if (!finished) {
                isMoving = true
                return@CameraListener
            }
            isMoving = false
            isLoading = true
            pickedPoint = position.target
            cameraZoom = position.zoom
            session?.cancel()
            session = searchManager.submit(
                position.target,
                position.zoom.roundToInt(),
                searchOptions,
                searchListener
            )
        }
    }

    fun selectSuggestion(suggestion: AddressSuggestion) {
        focusManager.clearFocus()
        userMoved.value = true
        query = ""
        suggestions = emptyList()
        noSuggestions = false
        mapView.mapWindow.map.move(CameraPosition(suggestion.point, SEARCH_RESULT_ZOOM, 0f, 0f))
    }

    fun moveTo(point: GeoPoint) {
        mapView.mapWindow.map.move(CameraPosition(Point(point.lat, point.lon), MY_LOCATION_ZOOM, 0f, 0f))
    }

    var locating by remember { mutableStateOf(false) }
    val locationRequester = rememberLocationRequester { outcome ->
        locating = false
        when (outcome) {
            is LocationOutcome.Found -> moveTo(outcome.point)
            LocationOutcome.PermissionBlocked ->
                Toast.makeText(context, R.string.text_12_44, Toast.LENGTH_LONG).show()
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        session = searchManager.submit(initialPoint, initialZoom.roundToInt(), searchOptions, searchListener)
        if (startPoint == null) {
            fetchLocationSilently(context) { point ->
                if (point != null && !userMoved.value) moveTo(point)
            }
        }
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
            session?.cancel()
            textSession?.cancel()
            if (started) {
                mapView.onStop()
                MapKitFactory.getInstance().onStop()
            }
        }
    }

    val pinLift by animateDpAsState(
        targetValue = if (isMoving) 14.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pinLift"
    )
    val shadowScale by animateFloatAsState(
        targetValue = if (isMoving) 0.6f else 1f,
        label = "pinShadow"
    )
    val addressAlpha by animateFloatAsState(
        targetValue = if (isMoving || isLoading) 0.45f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "addressAlpha"
    )

    val needsZoom = !forVolunteerLocation && cameraZoom < HOUSE_ZOOM
    val canPick = !isMoving && !isLoading && (address != null || forVolunteerLocation)
    val fallbackLabel = stringResource(R.string.text_12_34)
    val isDarkMap = MaterialTheme.colorScheme.background.luminance() < 0.5f

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
            ScreenHeader(
                title = stringResource(if (forVolunteerLocation) R.string.text_7_5 else R.string.text_7_1),
                onBack = onBack
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(FormRules.DEFAULT_MAX_LENGTH) },
                placeholder = { Text(stringResource(R.string.text_7_8)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { query = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        }
                    }
                } else {
                    null
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { suggestions.firstOrNull()?.let(::selectSuggestion) }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { searchFocused = it.isFocused }
            )
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
                            mapWindow.map.move(CameraPosition(initialPoint, initialZoom, 0f, 0f))
                            mapWindow.map.addCameraListener(WeakReference(cameraListener))
                        }
                    },
                    update = { it.mapWindow.map.isNightModeEnabled = isDarkMap },
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(width = 14.dp, height = 6.dp)
                        .graphicsLayer {
                            scaleX = shadowScale
                            scaleY = shadowScale
                        }
                        .background(Color.Black.copy(alpha = 0.25f), CircleShape)
                )

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-24).dp - pinLift)
                        .size(48.dp)
                )

                if (searchFocused && (suggestions.isNotEmpty() || noSuggestions)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.background,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            suggestions.forEach { suggestion ->
                                SuggestionRow(
                                    suggestion = suggestion,
                                    onClick = { selectSuggestion(suggestion) }
                                )
                            }
                            if (noSuggestions) {
                                Text(
                                    text = stringResource(R.string.text_7_9),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                }

                SmallFloatingActionButton(
                    onClick = {
                        if (!locating) {
                            locating = true
                            locationRequester.request()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    if (locating) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = stringResource(R.string.text_7_7)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(ADDRESS_BAR_HEIGHT)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                val text = address ?: stringResource(
                    if (failed) R.string.text_7_3 else R.string.text_7_2
                )
                AnimatedContent(
                    targetState = text,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
                    label = "address",
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(addressAlpha)
                ) { value ->
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        softWrap = false,
                        color = if (address != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                text = stringResource(
                    when {
                        forVolunteerLocation -> R.string.text_7_6
                        needsZoom -> R.string.text_7_10
                        else -> R.string.text_7_4
                    }
                ),
                enabled = if (needsZoom) !isMoving else canPick,
                onClick = {
                    if (needsZoom) {
                        mapView.mapWindow.map.move(
                            CameraPosition(pickedPoint, ZOOM_IN_TARGET, 0f, 0f),
                            Animation(Animation.Type.SMOOTH, 0.4f),
                            null
                        )
                    } else {
                        val label = address ?: if (forVolunteerLocation) fallbackLabel else null
                        label?.let { onPicked(it, GeoPoint(pickedPoint.latitude, pickedPoint.longitude)) }
                    }
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SuggestionRow(suggestion: AddressSuggestion, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (suggestion.subtitle.isNotBlank()) {
                Text(
                    text = suggestion.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}