package off.kys.backtalk.presentation.screen.bug

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import off.kys.backtalk.R
import kotlin.system.exitProcess

class BugScreen(
    private val exceptionName: String,
    private val message: String?,
    private val stackTrace: String,
    private val threadName: String = Thread.currentThread().name
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val activity = LocalActivity.current

        val notAvailableText = stringResource(R.string.common_not_available)
        val reportHeader = stringResource(R.string.bug_screen_report_header)
        val reportException = stringResource(R.string.bug_screen_report_exception, exceptionName)
        val reportMessage =
            stringResource(R.string.bug_screen_report_message, message ?: notAvailableText)
        val reportThread = stringResource(R.string.bug_screen_report_thread, threadName)
        val reportStackTraceLabel = stringResource(R.string.bug_screen_report_stack_trace_label)
        val shareSubject = stringResource(R.string.bug_screen_report_subject)
        val shareTitle = stringResource(R.string.bug_screen_share_report)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.bug_screen_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { activity?.finish() }) {
                            Icon(
                                painter = painterResource(R.drawable.round_close_24),
                                contentDescription = stringResource(R.string.common_close)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Exception Header Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.round_warning_24),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exceptionName.substringAfterLast('.'),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = exceptionName.substringBeforeLast('.', ""),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        if (!message.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = stringResource(R.string.bug_screen_thread_label, threadName),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Stack Trace Header
                Text(
                    text = stringResource(R.string.bug_screen_stack_trace_header),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )

                // Code/Stacktrace Terminal View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = MaterialTheme.shapes.large
                        )
                        .padding(16.dp)
                ) {
                    val traceScrollStateY = rememberScrollState()
                    val traceScrollStateX = rememberScrollState()
                    val codeColors = getCodeColors()

                    SelectionContainer {
                        Text(
                            text = formatStackTrace(stackTrace, codeColors),
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .verticalScroll(traceScrollStateY)
                                .horizontalScroll(traceScrollStateX)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions Container
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val report = """
                                $reportHeader
                                $reportException
                                $reportMessage
                                $reportThread
                                
                                $reportStackTraceLabel
                                $stackTrace
                            """.trimIndent()

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            activity?.startActivity(Intent.createChooser(shareIntent, shareTitle))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(painterResource(R.drawable.round_send_24), contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.bug_screen_share_report),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val intent =
                                activity?.packageManager?.getLaunchIntentForPackage(activity.packageName)
                            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            activity?.startActivity(intent)
                            exitProcess(0)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(
                            painterResource(R.drawable.round_refresh_24),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.bug_screen_restart_app),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }

    private data class CodeColors(
        val appCode: Color,
        val systemCode: Color,
        val errorTrace: Color,
        val defaultText: Color
    )

    @Composable
    private fun getCodeColors(): CodeColors {
        val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        return if (isDark) {
            CodeColors(
                appCode = Color(0xFFE5C07B),
                systemCode = Color(0xFF61AFEF),
                errorTrace = Color(0xFFE06C75),
                defaultText = Color(0xFFABB2BF)
            )
        } else {
            CodeColors(
                appCode = Color(0xFFB03060),
                systemCode = Color(0xFF006699),
                errorTrace = Color(0xFFD32F2F),
                defaultText = Color(0xFF24292E)
            )
        }
    }

    private fun formatStackTrace(trace: String, colors: CodeColors): AnnotatedString {
        return buildAnnotatedString {
            trace.lineSequence().forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("at off.kys.backtalk") -> {
                        withStyle(
                            SpanStyle(
                                color = colors.appCode,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append(line)
                        }
                    }

                    trimmed.startsWith("at ") -> {
                        withStyle(SpanStyle(color = colors.systemCode)) {
                            append(line)
                        }
                    }

                    trimmed.startsWith("Caused by:") || trimmed.contains("Exception") -> {
                        withStyle(
                            SpanStyle(
                                color = colors.errorTrace,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(line)
                        }
                    }

                    else -> {
                        withStyle(SpanStyle(color = colors.defaultText)) {
                            append(line)
                        }
                    }
                }
                append("\n")
            }
        }
    }
}