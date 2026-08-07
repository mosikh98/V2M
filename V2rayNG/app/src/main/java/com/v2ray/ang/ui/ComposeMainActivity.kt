package com.v2ray.ang.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.lifecycleScope
import com.v2ray.ang.AngApplication
import com.v2ray.ang.AppConfig
import com.v2ray.ang.core.LauncherManager
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.enums.EConfigType
import com.v2ray.ang.extension.toastError
import com.v2ray.ang.handler.SettingsManager
import com.v2ray.ang.ui.base.HelperBaseComponentActivity
import com.v2ray.ang.ui.main.ImportMenuContent
import com.v2ray.ang.ui.main.MainAction
import com.v2ray.ang.ui.main.MainRepository
import com.v2ray.ang.ui.main.MainViewModel
import com.v2ray.ang.ui.main.MoreMenuContent
import com.v2ray.ang.ui.screen.ActiveServerUi
import com.v2ray.ang.ui.screen.ConfigProtocol
import com.v2ray.ang.ui.screen.ConnectionState
import com.v2ray.ang.ui.screen.GroupItemUi
import com.v2ray.ang.ui.screen.GroupsScreen
import com.v2ray.ang.ui.screen.HomeScreen
import com.v2ray.ang.ui.screen.ProfileScreen
import com.v2ray.ang.ui.screen.SettingScreen
import com.v2ray.ang.ui.screen.demoAdvancedItems
import com.v2ray.ang.ui.screen.demoAppearanceItems
import com.v2ray.ang.ui.screen.demoConnectionToggles
import com.v2ray.ang.ui.screen.filterByProtocol
import com.v2ray.ang.ui.screen.toConfigItemUi
import com.v2ray.ang.ui.server.ProfileEditorResult
import com.v2ray.ang.ui.server.ServerCustomConfigActivity
import com.v2ray.ang.ui.server.ServerGroupActivity
import com.v2ray.ang.ui.server.ServerHttpActivity
import com.v2ray.ang.ui.server.ServerHysteria2Activity
import com.v2ray.ang.ui.server.ServerProxyChainActivity
import com.v2ray.ang.ui.server.ServerShadowsocksActivity
import com.v2ray.ang.ui.server.ServerSocksActivity
import com.v2ray.ang.ui.server.ServerTrojanActivity
import com.v2ray.ang.ui.server.ServerVlessActivity
import com.v2ray.ang.ui.server.ServerVmessActivity
import com.v2ray.ang.ui.server.ServerWireguardActivity
import com.v2ray.ang.ui.theme.BackgroundDark
import com.v2ray.ang.ui.theme.PrimaryBlue
import com.v2ray.ang.ui.theme.PrimaryBluePale
import com.v2ray.ang.ui.theme.SurfaceDark
import com.v2ray.ang.ui.theme.TextDisabled
import com.v2ray.ang.ui.theme.V2MTheme
import com.v2ray.ang.util.LogUtil
import com.v2ray.ang.util.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * تب‌های پایین صفحه.
 */
private enum class BottomTab(val label: String, val icon: ImageVector) {
    HOME("خانه", Icons.Filled.Home),
    PROFILES("کانفیگ‌ها", Icons.Filled.Layers),
    GROUPS("گروه‌ها", Icons.Filled.Group),
    SETTINGS("تنظیمات", Icons.Filled.Settings)
}

/**
 * اکتیویتی اصلی UI جدید. الان کاملاً هم‌ارز MainActivity قدیمیه:
 * - دکمه‌ی + همون ImportMenuContent واقعی رو داره (QR/کلیپ‌بورد/فایل/دستی برای هر پروتکل)
 * - دکمه‌ی سه‌نقطه همون MoreMenuContent واقعی رو داره (حذف همه/تکراری/نامعتبر، export،
 *   locate، sort، ping همه، real ping همه، آپدیت اشتراک‌ها، restart سرویس)
 * - دکمه‌ی Connect منطق واقعی VpnService.prepare + LauncherManager رو داره
 * - صفحه‌ی گروه‌ها الان لیست واقعی اشتراک‌هاست
 *
 * TODO باقی‌مونده: پرچم سرور فعال از SpeedtestManager.getRemoteIPInfo، آمار زنده‌ی
 * دانلود/آپلود، منوی share/edit/delete برای هر آیتم لیست، و وصل کردن سوییچ‌های تنظیمات.
 */
class ComposeMainActivity : HelperBaseComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application, MainRepository(application as AngApplication))
    }

    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) startV2Ray()
        }

    private val profileEditorLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) return@registerForActivityResult
            val data = result.data ?: return@registerForActivityResult
            val action = data.getStringExtra(ProfileEditorResult.EXTRA_ACTION)
                ?: return@registerForActivityResult
            if (action != ProfileEditorResult.ACTION_SAVED &&
                action != ProfileEditorResult.ACTION_DELETED
            ) return@registerForActivityResult
            val restartService = data.getBooleanExtra(
                ProfileEditorResult.EXTRA_RESTART_SERVICE, false
            )
            mainViewModel.onAction(MainAction.RefreshGroups)
            if (restartService && mainViewModel.uiState.value.isRunning) {
                restartV2Ray()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.onAction(MainAction.Initialize)
    }

    @Composable
    override fun ScreenContent() {
        V2MTheme {
            V2MApp(
                mainViewModel = mainViewModel,
                onToggleConnection = { handleFabAction() },
                onAction = { handleMainAction(it) }
            )
        }
    }

    private fun handleMainAction(action: MainAction) {
        when (action) {
            MainAction.ToggleService -> handleFabAction()
            MainAction.TestCurrentServer -> {
                if (mainViewModel.uiState.value.isRunning) {
                    mainViewModel.testCurrentServerRealPing()
                }
            }
            MainAction.ImportQRcode -> importQrCode()
            MainAction.ImportClipboard -> importClipboard()
            MainAction.ImportConfigLocal -> importFile()
            is MainAction.ImportManually -> importManually(action.type)
            MainAction.RestartService -> restartV2Ray()
            MainAction.LocateSelectedServer -> mainViewModel.triggerLocateSelectedServer()
            is MainAction.EditServer -> editServer(action.guid, action.profile)
            else -> mainViewModel.onAction(action)
        }
    }

    private fun handleFabAction() {
        if (mainViewModel.uiState.value.isRunning) {
            LauncherManager.stopService(this)
        } else if (SettingsManager.isVpnMode()) {
            val intent = VpnService.prepare(this)
            if (intent == null) startV2Ray() else requestVpnPermission.launch(intent)
        } else {
            startV2Ray()
        }
    }

    private fun startV2Ray() {
        if (mainViewModel.uiState.value.selectedGuid.isNullOrEmpty()) {
            toastError("یک کانفیگ رو اول از تب کانفیگ‌ها انتخاب کن")
            return
        }
        LauncherManager.startService(this)
    }

    private fun restartV2Ray() {
        if (mainViewModel.uiState.value.isRunning) LauncherManager.stopService(this)
        lifecycleScope.launch {
            delay(500)
            startV2Ray()
        }
    }

    private fun importClipboard() {
        try {
            val text = Utils.getClipboard(this)
            mainViewModel.onAction(MainAction.ImportBatchConfig(text))
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "Failed to import config from clipboard", e)
        }
    }

    private fun importQrCode() {
        launchQRCodeScanner { scanResult ->
            if (scanResult != null) {
                mainViewModel.onAction(MainAction.ImportBatchConfig(scanResult))
            }
        }
    }

    private fun importFile() {
        launchFileChooser { uri ->
            if (uri == null) return@launchFileChooser
            try {
                contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                    mainViewModel.onAction(MainAction.ImportBatchConfig(reader.readText()))
                }
            } catch (e: Exception) {
                LogUtil.e(AppConfig.TAG, "Failed to read content from URI", e)
            }
        }
    }

    private fun importManually(createConfigType: Int) {
        val intent = when (createConfigType) {
            EConfigType.POLICYGROUP.value -> Intent(this, ServerGroupActivity::class.java)
            EConfigType.PROXYCHAIN.value -> Intent(this, ServerProxyChainActivity::class.java)
            EConfigType.VMESS.value -> Intent(this, ServerVmessActivity::class.java)
            EConfigType.VLESS.value -> Intent(this, ServerVlessActivity::class.java)
            EConfigType.SHADOWSOCKS.value -> Intent(this, ServerShadowsocksActivity::class.java)
            EConfigType.SOCKS.value -> Intent(this, ServerSocksActivity::class.java)
            EConfigType.HTTP.value -> Intent(this, ServerHttpActivity::class.java)
            EConfigType.TROJAN.value -> Intent(this, ServerTrojanActivity::class.java)
            EConfigType.WIREGUARD.value -> Intent(this, ServerWireguardActivity::class.java)
            EConfigType.HYSTERIA2.value -> Intent(this, ServerHysteria2Activity::class.java)
            else -> Intent(this, ServerHttpActivity::class.java).apply {
                putExtra("createConfigType", createConfigType)
            }
        }.apply {
            putExtra("subscriptionId", mainViewModel.uiState.value.selectedGroupId)
        }
        profileEditorLauncher.launch(intent)
    }

    private fun editServer(guid: String, profile: ProfileItem) {
        val activityClass = when (profile.configType) {
            EConfigType.CUSTOM -> ServerCustomConfigActivity::class.java
            EConfigType.POLICYGROUP -> ServerGroupActivity::class.java
            EConfigType.PROXYCHAIN -> ServerProxyChainActivity::class.java
            EConfigType.VMESS -> ServerVmessActivity::class.java
            EConfigType.VLESS -> ServerVlessActivity::class.java
            EConfigType.SHADOWSOCKS -> ServerShadowsocksActivity::class.java
            EConfigType.SOCKS -> ServerSocksActivity::class.java
            EConfigType.HTTP -> ServerHttpActivity::class.java
            EConfigType.TROJAN -> ServerTrojanActivity::class.java
            EConfigType.WIREGUARD -> ServerWireguardActivity::class.java
            EConfigType.HYSTERIA2 -> ServerHysteria2Activity::class.java
            else -> ServerHttpActivity::class.java
        }
        val intent = Intent(this, activityClass).apply {
            putExtra("guid", guid)
            putExtra("isRunning", mainViewModel.uiState.value.isRunning)
            putExtra("createConfigType", profile.configType.value)
            putExtra("subscriptionId", mainViewModel.uiState.value.selectedGroupId)
        }
        profileEditorLauncher.launch(intent)
    }
}

@Composable
private fun V2MApp(
    mainViewModel: MainViewModel,
    onToggleConnection: () -> Unit,
    onAction: (MainAction) -> Unit
) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var selectedProtocol by remember { mutableStateOf(ConfigProtocol.ALL) }

    var showDelAllConfirm by remember { mutableStateOf(false) }
    var showDelDuplicateConfirm by remember { mutableStateOf(false) }
    var showDelInvalidConfirm by remember { mutableStateOf(false) }

    val uiState by mainViewModel.uiState.collectAsState()
    val realServers by mainViewModel
        .serversForGroup(uiState.selectedGroupId)
        .collectAsState()

    val filteredConfigs = remember(selectedProtocol, realServers, uiState.selectedGuid) {
        realServers
            .filterByProtocol(selectedProtocol)
            .map { it.toConfigItemUi(isSelected = it.guid == uiState.selectedGuid) }
    }

    val activeServerItem = remember(realServers, uiState.selectedGuid) {
        realServers.firstOrNull { it.guid == uiState.selectedGuid }
    }
    val connectionState = if (uiState.isRunning) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED

    if (showDelAllConfirm) {
        ConfirmDialog(
            text = "همه‌ی کانفیگ‌ها حذف بشن؟",
            onConfirm = { showDelAllConfirm = false; onAction(MainAction.RemoveAllServers) },
            onDismiss = { showDelAllConfirm = false }
        )
    }
    if (showDelDuplicateConfirm) {
        ConfirmDialog(
            text = "کانفیگ‌های تکراری حذف بشن؟",
            onConfirm = { showDelDuplicateConfirm = false; onAction(MainAction.RemoveDuplicateServers) },
            onDismiss = { showDelDuplicateConfirm = false }
        )
    }
    if (showDelInvalidConfirm) {
        ConfirmDialog(
            text = "کانفیگ‌های نامعتبر حذف بشن؟",
            onConfirm = { showDelInvalidConfirm = false; onAction(MainAction.RemoveInvalidServers) },
            onDismiss = { showDelInvalidConfirm = false }
        )
    }

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            V2MBottomBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        }
    ) { padding ->
        when (selectedTab) {
            BottomTab.HOME -> HomeScreen(
                modifier = Modifier.padding(padding),
                connectionState = connectionState,
                activeServer = activeServerItem?.let { server ->
                    val ui = server.toConfigItemUi(isSelected = true)
                    ActiveServerUi(
                        countryFlag = ui.countryFlag,
                        name = ui.name,
                        protocol = ui.protocolLabel,
                        pingMs = ui.pingMs
                    )
                },
                downloadBytes = "—",
                uploadBytes = "—",
                profileCount = realServers.size,
                groupCount = uiState.groups.size,
                onConnectToggle = onToggleConnection,
                onOpenProfiles = { selectedTab = BottomTab.PROFILES },
                onOpenGroups = { selectedTab = BottomTab.GROUPS },
                onOpenSettings = { selectedTab = BottomTab.SETTINGS },
                onOpenLogs = { /* TODO: باز کردن LogcatActivity */ }
            )

            BottomTab.PROFILES -> ProfileScreen(
                modifier = Modifier.padding(padding),
                configs = filteredConfigs,
                selectedProtocol = selectedProtocol,
                onProtocolSelected = { selectedProtocol = it },
                onConfigClick = { config ->
                    onAction(MainAction.SelectServer(config.id))
                },
                onConfigPing = { config ->
                    onAction(MainAction.SelectServer(config.id))
                    onAction(MainAction.TestCurrentServer)
                },
                onOpenMenu = { /* TODO: باز کردن drawer یا منو */ },
                onSearch = { /* TODO: باز کردن سرچ */ },
                onFilter = { /* TODO: باز کردن فیلتر */ },
                importMenuContent = { ImportMenuContent(onAction = onAction) },
                moreMenuContent = {
                    MoreMenuContent(
                        onAction = onAction,
                        onDelAllConfig = { showDelAllConfirm = true },
                        onDelDuplicateConfig = { showDelDuplicateConfirm = true },
                        onDelInvalidConfig = { showDelInvalidConfirm = true }
                    )
                }
            )

            BottomTab.GROUPS -> GroupsScreen(
                modifier = Modifier.padding(padding),
                groups = uiState.groups.map { group ->
                    GroupItemUi(
                        id = group.id,
                        remarks = group.remarks,
                        isSelected = group.id == uiState.selectedGroupId
                    )
                },
                onGroupClick = { group ->
                    onAction(MainAction.SelectGroup(group.id))
                    selectedTab = BottomTab.PROFILES
                },
                onOpenMenu = { /* TODO: باز کردن drawer یا منو */ }
            )

            BottomTab.SETTINGS -> SettingScreen(
                modifier = Modifier.padding(padding),
                connectionToggles = demoConnectionToggles(),
                appearanceItems = demoAppearanceItems(),
                advancedItems = demoAdvancedItems(),
                onToggleChanged = { _, _ -> /* TODO: ذخیره تو MmkvManager */ },
                onNavItemClick = { /* TODO: باز کردن صفحه‌ی مربوطه */ },
                onOpenMenu = { /* TODO: باز کردن drawer یا منو */ }
            )
        }
    }
}

@Composable
private fun ConfirmDialog(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تأیید") },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("تأیید") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

@Composable
private fun V2MBottomBar(selectedTab: BottomTab, onTabSelected: (BottomTab) -> Unit) {
    NavigationBar(containerColor = SurfaceDark) {
        BottomTab.values().forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBluePale,
                    unselectedIconColor = TextDisabled,
                    unselectedTextColor = TextDisabled,
                    indicatorColor = SurfaceDark
                )
            )
        }
    }
}
