package off.kys.backtalk.presentation.screen.messages.components

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.domain.model.MediaItem
import off.kys.backtalk.presentation.screen.camera.CameraCaptureScreen
import off.kys.backtalk.util.emptyString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPickerSheet(
    onMediaSelected: (selectedMedia: List<Uri>, mediaType: String, captionText: String?) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
) {
    val navigator = LocalNavigator.currentOrThrow
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedUris by remember { mutableStateOf(emptySet<Uri>()) }
    var selectedType by remember { mutableStateOf("image/jpeg") }
    var captionText by remember { mutableStateOf(emptyString()) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val mediaPermissions = remember {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissions.toTypedArray()
    }

    var hasMediaPermission by remember {
        mutableStateOf(
            mediaPermissions.any {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasMediaPermission = mediaPermissions.any {
            permissions[it] ?: (ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission || !hasMediaPermission) {
            val toRequest = mutableListOf(Manifest.permission.CAMERA)
            toRequest.addAll(mediaPermissions)
            permissionLauncher.launch(toRequest.toTypedArray())
        }
    }

    val mediaItems = remember(hasMediaPermission) {
        if (hasMediaPermission) fetchGalleryMedia(context) else emptyList()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(36.dp, 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 80.dp, start = 4.dp, end = 4.dp)
            ) {
                item(span = { GridItemSpan(3) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 2f)
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .aspectRatio(1f)
                        ) {
                            if (hasCameraPermission) {
                                CameraPreviewItem(
                                    onClick = {
                                        onDismiss()
                                        navigator.push(
                                            CameraCaptureScreen { uri ->
                                                onMediaSelected(
                                                    listOf(uri),
                                                    "image/jpeg",
                                                    null
                                                )
                                            }
                                        )
                                    }
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = stringResource(R.string.camera_blocked),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f / 2f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            mediaItems.take(2).fastForEach { item ->
                                val uriIndex = selectedUris.indexOf(item.uri)
                                GalleryItem(
                                    item = item,
                                    showSelection = selectedUris.isNotEmpty(),
                                    selectedIndex = if (uriIndex != -1) uriIndex + 1 else 0,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    onSelected = { uri, type ->
                                        selectedType = type
                                        val isAdding = uri !in selectedUris
                                        selectedUris = if (!isAdding) {
                                            linkedSetOf<Uri>().apply { addAll(selectedUris.filter { it != uri }) }
                                        } else {
                                            linkedSetOf<Uri>().apply { addAll(selectedUris); add(uri) }
                                        }
                                        if (isAdding && sheetState.currentValue != SheetValue.Expanded) {
                                            scope.launch { sheetState.expand() }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                itemsIndexed(mediaItems.drop(2)) { _, item ->
                    val uriIndex = selectedUris.indexOf(item.uri)
                    GalleryItem(
                        item = item,
                        showSelection = selectedUris.isNotEmpty(),
                        selectedIndex = if (uriIndex != -1) uriIndex + 1 else 0,
                        modifier = Modifier.aspectRatio(1f),
                        onSelected = { uri, type ->
                            selectedType = type
                            val isAdding = uri !in selectedUris
                            selectedUris = if (!isAdding) {
                                linkedSetOf<Uri>().apply { addAll(selectedUris.filter { it != uri }) }
                            } else {
                                linkedSetOf<Uri>().apply { addAll(selectedUris); add(uri) }
                            }
                            if (isAdding && sheetState.currentValue != SheetValue.Expanded) {
                                scope.launch { sheetState.expand() }
                            }
                        }
                    )
                }
            }

            this@ModalBottomSheet.AnimatedVisibility(
                visible = selectedUris.isNotEmpty(),
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        textStyle = TextStyle(textDirection = TextDirection.Content),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            onMediaSelected(
                                selectedUris.toList(),
                                selectedType,
                                captionText.ifBlank { null })
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.common_send),
                                style = MaterialTheme.typography.labelLarge
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = selectedUris.size.toString(),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Icon(
                                painter = painterResource(R.drawable.round_send_24),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
            } catch (_: Exception) {
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = this.surfaceProvider
                            }

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview
                            )
                        } catch (e: Exception) {
                            Log.e("CameraPreview", "Use case binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryItem(
    item: MediaItem,
    showSelection: Boolean,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelected: (Uri, String) -> Unit
) {
    val isSelected = selectedIndex > 0

    val itemScale by animateFloatAsState(
        targetValue = if (isSelected) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "itemScale"
    )

    Box(
        modifier = modifier
            .scale(itemScale)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .combinedClickable(
                onClick = { onSelected(item.uri, item.type) },
                onLongClick = { }
            )
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(4.dp)
                )
                else Modifier
            )
    ) {
        Image(
            painter = rememberAsyncImagePainter(item.uri),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent,
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
        ) {
            val bubbleScale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.8f,
                animationSpec = tween(durationMillis = 100),
                label = "bubbleScale"
            )

            AnimatedVisibility(
                visible = showSelection,
                enter = fadeIn() + scaleIn(initialScale = 0.8f),
                exit = fadeOut() + scaleOut(targetScale = 0.8f)
            ) {
                Box(
                    modifier = Modifier
                        .scale(bubbleScale)
                        .size(24.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(
                                alpha = 0.35f
                            ),
                            shape = CircleShape
                        )
                        .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Text(
                            text = selectedIndex.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }

        if (item.type == "image/gif") {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp),
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "GIF",
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun fetchGalleryMedia(context: Context): List<MediaItem> {
    val items = mutableListOf<MediaItem>()
    val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.MIME_TYPE
    )
    val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"
    val queryUri = MediaStore.Files.getContentUri("external")
    val selection =
        "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}) OR (${MediaStore.MediaColumns.MIME_TYPE} = ?)"
    val selectionArgs = arrayOf("image/svg+xml")

    context.contentResolver.query(queryUri, projection, selection, selectionArgs, sortOrder)
        ?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val mimeType = cursor.getString(mimeColumn)
                val uri = ContentUris.withAppendedId(queryUri, id)
                items.add(MediaItem(uri, mimeType))
            }
        }
    return items
}