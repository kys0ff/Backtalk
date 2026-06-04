package off.kys.backtalk.presentation.screen.preview

import android.app.Activity
import android.content.Intent
import android.webkit.URLUtil
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil.compose.AsyncImage
import off.kys.backtalk.R
import off.kys.backtalk.presentation.components.HintTooltip
import java.io.File

class ImagePreviewScreen(val imagePath: String) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current

        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        var size by remember { mutableStateOf(IntSize.Zero) }

        var isUiVisible by remember { mutableStateOf(true) }

        val window = (context as? Activity)?.window

        LaunchedEffect(isUiVisible, window) {
            window?.let { win ->
                WindowCompat.setDecorFitsSystemWindows(win, false)

                val insetsController = WindowCompat.getInsetsController(win, win.decorView)
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                if (isUiVisible) {
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                } else {
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                }
            }
        }

        val animatedScale by animateFloatAsState(
            targetValue = scale,
            animationSpec = tween(durationMillis = 250),
            label = "ScaleAnimation"
        )
        val animatedOffsetX by animateFloatAsState(
            targetValue = offsetX,
            animationSpec = tween(durationMillis = 250),
            label = "OffsetXAnimation"
        )
        val animatedOffsetY by animateFloatAsState(
            targetValue = offsetY,
            animationSpec = tween(durationMillis = 250),
            label = "OffsetYAnimation"
        )

        Scaffold(
            topBar = {
                AnimatedVisibility(
                    visible = isUiVisible,
                    enter = slideInVertically(initialOffsetY = { -it }),
                    exit = slideOutVertically(targetOffsetY = { -it })
                ) {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            HintTooltip(stringResource(R.string.common_navigate_up)) {
                                IconButton(onClick = { navigator.pop() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.round_arrow_back_24),
                                        contentDescription = stringResource(R.string.common_navigate_up),
                                    )
                                }
                            }
                        },
                        actions = {
                            HintTooltip(stringResource(R.string.common_share)) {
                                IconButton(
                                    onClick = {
                                        if (URLUtil.isNetworkUrl(imagePath)) {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, imagePath)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, null))
                                        } else {
                                            val file = File(imagePath)
                                            if (file.exists()) {
                                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "image/*"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(Intent.createChooser(shareIntent, null))
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.round_share_24),
                                        contentDescription = stringResource(R.string.common_share),
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black.copy(alpha = 0.5f)
                        )
                    )
                }
            },
            containerColor = Color.Black
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isUiVisible) innerPadding else PaddingValues())
                    .background(Color.Black)
                    .onGloballyPositioned { coordinates ->
                        size = coordinates.size
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                isUiVisible = !isUiVisible
                            },
                            onDoubleTap = {
                                if (scale > 1f) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    scale = 2.5f
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)

                            if (scale > 1f) {
                                val maxOffsetX = (size.width * (scale - 1f)) / 2f
                                val maxOffsetY = (size.height * (scale - 1f)) / 2f

                                offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = if (imagePath.startsWith("/")) File(imagePath) else imagePath,
                    contentDescription = stringResource(R.string.image_preview_visual_preview),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                            translationX = animatedOffsetX
                            translationY = animatedOffsetY
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}