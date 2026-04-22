package off.kys.backtalk.presentation.screen.preferences

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.R
import off.kys.backtalk.common.Constants
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.presentation.activity.MainActivity
import off.kys.backtalk.util.isSecurityEnabled
import off.kys.backtalk.util.openUrl
import off.kys.backtalk.util.toast
import org.koin.compose.koinInject

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val mainActivity = LocalActivity.current as MainActivity
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val navigator = LocalNavigator.currentOrThrow
        val prefs = koinInject<BacktalkPreferences>()
        val context = LocalContext.current

        var themeMode by remember { mutableStateOf(prefs.themeMode) }
        var dynamicColor by remember { mutableStateOf(prefs.dynamicColorEnabled) }
        var lockEnabled by remember { mutableStateOf(prefs.lockEnabled) }
        var secureScreen by remember { mutableStateOf(prefs.secureScreenEnabled) }
        var autoUpdate by remember { mutableStateOf(prefs.autoUpdateEnabled) }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                SettingsTopAppBar(
                    onNavigateBack = { navigator.pop() },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                PreferenceCategory(stringResource(R.string.appearance))

                ThemeSelector(
                    selected = themeMode,
                    onSelected = {
                        prefs.themeMode = it
                        themeMode = it
                    }
                )

                ToggleSetting(
                    label = stringResource(R.string.material_you_dynamic_color),
                    supportingText = stringResource(R.string.apply_system_colors_to_the_app_interface),
                    icon = painterResource(R.drawable.round_palette_24),
                    checked = dynamicColor,
                    onCheckedChange = {
                        prefs.dynamicColorEnabled = it
                        dynamicColor = it
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                PreferenceCategory(stringResource(R.string.privacy_security))

                if (context.isSecurityEnabled()) {
                    ToggleSetting(
                        label = stringResource(R.string.enable_app_lock),
                        icon = painterResource(R.drawable.round_lock_24),
                        checked = lockEnabled,
                        requireRestart = true,
                        onCheckedChange = {
                            prefs.lockEnabled = it
                            lockEnabled = it
                        }
                    )
                }

                ToggleSetting(
                    label = stringResource(R.string.secure_screen_block_screenshots),
                    icon = painterResource(R.drawable.round_screen_lock_portrait_24),
                    checked = secureScreen,
                    onCheckedChange = {
                        prefs.secureScreenEnabled = it
                        secureScreen = it
                    }
                )

                if (!BuildConfig.IS_FDROID) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                    PreferenceCategory(stringResource(R.string.updates))

                    ToggleSetting(
                        label = stringResource(R.string.auto_check_updates),
                        supportingText = stringResource(R.string.auto_check_updates_desc),
                        icon = painterResource(R.drawable.round_update_24),
                        checked = autoUpdate,
                        onCheckedChange = {
                            prefs.autoUpdateEnabled = it
                            autoUpdate = it
                        }
                    )

                    InfoRow(
                        label = stringResource(R.string.check_for_updates_now),
                        value = stringResource(R.string.tap_to_check_latest_version),
                        icon = painterResource(R.drawable.round_refresh_24),
                        onClick = {
                            context.toast(R.string.checking_for_updates)
                            mainActivity.checkForUpdates()
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                PreferenceCategory(stringResource(R.string.about))

                InfoRow(
                    label = stringResource(R.string.version),
                    value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    icon = painterResource(R.drawable.round_info_24)
                )

                InfoRow(
                    label = stringResource(R.string.developer),
                    value = stringResource(R.string.dev_name),
                    icon = painterResource(R.drawable.round_person_24),
                    onClick = { context.toast(R.string.dev_click) }
                )

                InfoRow(
                    label = stringResource(R.string.license),
                    value = stringResource(R.string.mit),
                    icon = painterResource(R.drawable.round_description_24),
                    onClick = { context.openUrl(Constants.BACKTALK_MIT_LICENSE_RAW_URL) }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SettingsTopAppBar(
        onNavigateBack: () -> Unit,
        scrollBehavior: TopAppBarScrollBehavior
    ) {
        LargeTopAppBar(
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(R.drawable.round_arrow_back_24),
                        contentDescription = stringResource(R.string.navigate_up)
                    )
                }
            },
            title = { Text(text = stringResource(R.string.settings)) },
            scrollBehavior = scrollBehavior
        )
    }

    @Composable
    private fun PreferenceCategory(text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
        )
    }

    @Composable
    private fun ToggleSetting(
        label: String,
        icon: Painter? = null,
        supportingText: String? = null,
        checked: Boolean,
        requireRestart: Boolean = false,
        onCheckedChange: (Boolean) -> Unit
    ) {
        val context = LocalContext.current
        ListItem(
            headlineContent = { Text(label) },
            supportingContent = supportingText?.let { { Text(it) } },
            leadingContent = icon?.let { { Icon(it, contentDescription = null) } },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        onCheckedChange(it)
                        if (requireRestart) context.toast(R.string.restart_app_to_apply_changes)
                    }
                )
            },
            modifier = Modifier.clickable { onCheckedChange(!checked) }
        )
    }

    @Composable
    private fun InfoRow(
        label: String,
        value: String,
        icon: Painter? = null,
        onClick: (() -> Unit)? = null
    ) {
        ListItem(
            headlineContent = { Text(label) },
            supportingContent = { Text(value) },
            leadingContent = icon?.let { { Icon(it, contentDescription = null) } },
            modifier = Modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        )
    }

    @Composable
    private fun ThemeSelector(selected: ThemeMode, onSelected: (ThemeMode) -> Unit) {
        Column(Modifier.selectableGroup()) {
            ThemeMode.entries.forEach { mode ->
                ListItem(
                    headlineContent = {
                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                    },
                    leadingContent = {
                        RadioButton(
                            selected = (mode == selected),
                            onClick = null // Handled by ListItem click
                        )
                    },
                    modifier = Modifier.clickable { onSelected(mode) }
                )
            }
        }
    }
}