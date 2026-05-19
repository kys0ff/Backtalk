package off.kys.backtalk.presentation.screen.camera

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.MotionEvent
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil.compose.AsyncImage
import kotlinx.coroutines.suspendCancellableCoroutine
import off.kys.backtalk.R
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraCaptureScreen(
    private val onImageCaptured: (Uri) -> Unit
) : Screen {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
        var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
        var capturedUri by remember { mutableStateOf<Uri?>(null) }
        var isCapturing by remember { mutableStateOf(false) }

        val imageCapture = remember {
            ImageCapture.Builder().build()
        }

        LaunchedEffect(flashMode) {
            imageCapture.flashMode = flashMode
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {
            Crossfade(
                targetState = capturedUri != null,
                label = "CameraStateTransition"
            ) { isReviewing ->
                if (!isReviewing) {
                    ActiveCameraView(
                        lensFacing = lensFacing,
                        flashMode = flashMode,
                        imageCapture = imageCapture,
                        isCapturing = isCapturing,
                        onClose = { navigator.pop() },
                        onFlashToggle = {
                            flashMode = when (flashMode) {
                                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                                else -> ImageCapture.FLASH_MODE_OFF
                            }
                        },
                        onSwitchCamera = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        onCaptureClick = {
                            isCapturing = true
                            captureImage(context, imageCapture) { uri ->
                                isCapturing = false
                                capturedUri = uri
                            }
                        }
                    )
                } else {
                    ImageReviewView(
                        uri = capturedUri,
                        onRetake = { capturedUri = null },
                        onAccept = {
                            capturedUri?.let {
                                onImageCaptured(it)
                                navigator.pop()
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun ActiveCameraView(
        lensFacing: Int,
        flashMode: Int,
        imageCapture: ImageCapture,
        isCapturing: Boolean,
        onClose: () -> Unit,
        onFlashToggle: () -> Unit,
        onSwitchCamera: () -> Unit,
        onCaptureClick: () -> Unit
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                lensFacing = lensFacing,
                imageCapture = imageCapture
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onClose,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_close_24),
                        contentDescription = stringResource(R.string.common_close)
                    )
                }

                FilledIconButton(
                    onClick = onFlashToggle,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f),
                        contentColor = if (flashMode == ImageCapture.FLASH_MODE_OFF) Color.White else MaterialTheme.colorScheme.primary
                    )
                ) {
                    val icon = when (flashMode) {
                        ImageCapture.FLASH_MODE_ON -> R.drawable.round_flash_on_24
                        ImageCapture.FLASH_MODE_AUTO -> R.drawable.round_flash_auto_24
                        else -> R.drawable.round_flash_off_24
                    }
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = stringResource(R.string.camera_flash)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(56.dp))

                val shutterScale by animateFloatAsState(
                    targetValue = if (isCapturing) 0.8f else 1f,
                    label = "ShutterScale"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(shutterScale)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(4.dp, Color.White, CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isCapturing,
                            onClick = onCaptureClick
                        )
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = Color.White
                        ) {}
                    }
                }

                FilledIconButton(
                    onClick = onSwitchCamera,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_autorenew_24),
                        contentDescription = stringResource(R.string.camera_switch),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun ImageReviewView(
        uri: Uri?,
        onRetake: () -> Unit,
        onAccept: () -> Unit
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(0.3f)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onRetake,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.camera_retake),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_check_24),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.camera_send),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Composable
    private fun CameraPreview(
        modifier: Modifier,
        lensFacing: Int,
        imageCapture: ImageCapture
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val preview = remember { Preview.Builder().build() }
        val previewView = remember { PreviewView(context) }

        val cameraSelector = remember(lensFacing) {
            CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
        }

        // Drop 'imageCapture' from these keys.
        // Only rebind when switching between front and back cameras.
        LaunchedEffect(lensFacing) {
            val cameraProvider = context.getCameraProvider()
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            preview.surfaceProvider = previewView.surfaceProvider

            previewView.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    view.performClick()
                    val factory = previewView.meteringPointFactory
                    val point = factory.createPoint(event.x, event.y)
                    val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                        .build()
                    camera.cameraControl.startFocusAndMetering(action)
                    true
                } else {
                    false
                }
            }
        }

        AndroidView(factory = { previewView }, modifier = modifier)
    }

    private fun captureImage(
        context: Context,
        imageCapture: ImageCapture,
        onResult: (Uri) -> Unit
    ) {
        val outputDirectory = context.cacheDir
        val photoFile = File(outputDirectory, "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onResult(Uri.fromFile(photoFile))
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                }
            }
        )
    }

    private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
        suspendCancellableCoroutine { continuation ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

            cameraProviderFuture.addListener({
                try {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    val cameraProvider = cameraProviderFuture.get()
                    continuation.resume(cameraProvider)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, ContextCompat.getMainExecutor(this))

            continuation.invokeOnCancellation {
                cameraProviderFuture.cancel(true)
            }
        }
}