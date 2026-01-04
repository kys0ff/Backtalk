package off.kys.backtalk

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

// Define constants for grouping logic
private val TIME_GAP_FOR_HEADER = TimeUnit.HOURS.toMillis(1)
private val TIME_GAP_FOR_GROUPING = TimeUnit.MINUTES.toMillis(1)

class MessagingScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val database = koinInject<DatabaseProvider>().getDatabase()
        val dao = database.messagesDao()
        val messages by dao.getAllMessages().collectAsState(initial = emptyList())
        val sortedMessages = messages.sortedBy { it.timestamp }

        var replyingTo by remember { mutableStateOf<Message?>(null) }
        // State for message selection/deletion
        var selectedMessageId by remember { mutableStateOf<MessageId?>(null) }

        BackHandler(enabled = selectedMessageId != null) {
            selectedMessageId = null
        }

        BackHandler(enabled = replyingTo != null) {
            replyingTo = null
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (selectedMessageId != null)
                                stringResource(R.string.message_selected)
                            else stringResource(R.string.messages)
                        )
                    },
                    navigationIcon = {
                        if (selectedMessageId != null) {
                            IconButton(
                                onClick = {
                                    selectedMessageId = null
                                }
                            ) {
                                Icon(
                                    painterResource(R.drawable.round_close_24),
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    actions = {
                        if (selectedMessageId != null) {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    selectedMessageId?.let { dao.deleteMessageById(it) }
                                    selectedMessageId = null
                                }
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.round_delete_24),
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (selectedMessageId != null) {
                            TopAppBarDefaults.topAppBarColors().scrolledContainerColor
                        } else {
                            TopAppBarDefaults.topAppBarColors().containerColor
                        }
                    )
                )
            },
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    reverseLayout = true
                ) {
                    val reversedList = sortedMessages.reversed()
                    items(reversedList.size, key = { reversedList[it].id() }) { index ->
                        val current = reversedList[index]
                        val next = reversedList.getOrNull(index - 1)
                        val prev = reversedList.getOrNull(index + 1)

                        val showTimestamp =
                            prev == null || (current.timestamp - prev.timestamp > TIME_GAP_FOR_HEADER)
                        val isTop =
                            prev == null || (current.timestamp - prev.timestamp > TIME_GAP_FOR_GROUPING)
                        val isBottom =
                            next == null || (next.timestamp - current.timestamp > TIME_GAP_FOR_GROUPING)

                        val repliedMessage =
                            current.repliedToId?.let { id -> sortedMessages.find { it.id == id } }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (showTimestamp) {
                                TimestampHeader(current.timestamp)
                            }

                            SwipeToReplyWrapper(
                                onSwipe = {
                                    replyingTo = if (replyingTo == current) {
                                        null
                                    } else {
                                        current
                                    }
                                }
                            ) {
                                MessageBubble(
                                    message = current,
                                    repliedMessage = repliedMessage,
                                    isTop = isTop,
                                    isBottom = isBottom,
                                    isSelected = selectedMessageId == current.id,
                                    onLongClick = { selectedMessageId = current.id },
                                    onClick = {
                                        if (selectedMessageId != null) {
                                            selectedMessageId =
                                                if (selectedMessageId == current.id) null else current.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                InputBar(
                    replyingTo = replyingTo,
                    onCancelReply = { replyingTo = null },
                    onMessageSend = { text ->
                        val message = Message(
                            id = MessageId.generate(),
                            text = text,
                            timestamp = System.currentTimeMillis(),
                            repliedToId = replyingTo?.id
                        )
                        coroutineScope.launch { dao.insertMessage(message) }
                        replyingTo = null
                    }
                )
            }
        }
    }

    /**
     * A wrapper that adds swipe-to-trigger functionality.
     * Swiping right reveals a reply icon and triggers the callback.
     */
    @Composable
    fun SwipeToReplyWrapper(
        onSwipe: () -> Unit,
        content: @Composable () -> Unit
    ) {
        val vibrationManager = koinInject<VibrationManager>()
        val offsetX = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()
        val haptic = LocalHapticFeedback.current
        val density = LocalDensity.current

        val actionThreshold = with(density) { 60.dp.toPx() }
        val maxDrag = with(density) { 90.dp.toPx() }

        val progress = (offsetX.value / actionThreshold).coerceIn(0f, 1f)
        val isPastThreshold = offsetX.value >= actionThreshold

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {

                    // 🔹 Gesture-local state (NOT Compose state)
                    var hasVibratedThreshold = false

                    detectHorizontalDragGestures(
                        onDragStart = {
                            hasVibratedThreshold = false
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value >= actionThreshold) {
                                    onSwipe()
                                }
                                offsetX.animateTo(
                                    0f,
                                    spring(
                                        Spring.DampingRatioLowBouncy,
                                        Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetX.animateTo(0f) }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            val newOffset =
                                (offsetX.value + dragAmount).coerceIn(0f, maxDrag)

                            if (newOffset >= actionThreshold && !hasVibratedThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vibrationManager.vibrate()
                                hasVibratedThreshold = true
                            } else if (newOffset < actionThreshold) {
                                hasVibratedThreshold = false
                            }

                            scope.launch { offsetX.snapTo(newOffset) }
                            change.consume()
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .alpha(progress)
                    .scale(0.6f + (0.4f * progress))
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_reply_24),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isPastThreshold)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier.offset {
                    IntOffset(offsetX.value.roundToInt(), 0)
                }
            ) {
                content()
            }
        }
    }

    @Composable
    fun InputBar(
        replyingTo: Message?,
        onCancelReply: () -> Unit,
        onMessageSend: (String) -> Unit
    ) {
        var textState by remember { mutableStateOf("") }

        Surface(tonalElevation = 2.dp) {
            Column(modifier = Modifier.navigationBarsPadding()) {
                AnimatedVisibility(
                    visible = replyingTo != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.replying_to),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                replyingTo?.text ?: "",
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = onCancelReply, modifier = Modifier.size(24.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.round_close_24),
                                contentDescription = stringResource(R.string.cancel)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.type_a_message)) },
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    )

                    IconButton(
                        onClick = {
                            if (textState.isNotBlank()) {
                                onMessageSend(textState)
                                textState = ""
                            }
                        },
                        enabled = textState.isNotBlank()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.round_send_24),
                            contentDescription = stringResource(R.string.send),
                            tint = if (textState.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun TimestampHeader(timestamp: Long) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        Text(
            text = sdf.format(Date(timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun MessageBubble(
        message: Message,
        repliedMessage: Message?,
        isTop: Boolean,
        isBottom: Boolean,
        isSelected: Boolean,
        onClick: () -> Unit,
        onLongClick: () -> Unit
    ) {
        var showTime by remember { mutableStateOf(false) }
        val haptic = LocalHapticFeedback.current

        // Remove ripple by passing null to indication
        val interactionSource = remember { MutableInteractionSource() }

        val bubbleColor = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.primary
        }

        val bubbleShape = RoundedCornerShape(
            topStart = 18.dp,
            topEnd = if (isTop) 18.dp else 4.dp,
            bottomEnd = if (isBottom) 18.dp else 4.dp,
            bottomStart = 18.dp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (isTop) 6.dp else 0.dp),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                color = bubbleColor,
                shape = bubbleShape,
                modifier = Modifier.combinedClickable(
                    interactionSource = interactionSource,
                    indication = null, // This removes the ripple effect
                    onClick = {
                        if (!isSelected) showTime = !showTime
                        onClick()
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    if (repliedMessage != null) {
                        ReplyPreview(repliedMessage.text)
                    }
                    Text(
                        text = message.text,
                        color = contentColorFor(bubbleColor)
                    )
                }
            }
            // ... (Timestamp visibility logic remains same)
            AnimatedVisibility(
                visible = showTime,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = SimpleDateFormat(
                        "h:mm a",

                        Locale.getDefault()
                    ).format(Date(message.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, end = 4.dp)
                )
            }
        }
    }

    @Composable
    private fun ReplyPreview(text: String) {
        Row(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .height(IntrinsicSize.Min)
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.5f))
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}