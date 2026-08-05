package com.v2ray.ang.ui

import android.app.Activity.RESULT_OK
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.v2ray.ang.AngApplication
import com.v2ray.ang.AppConfig
import com.v2ray.ang.extension.toastError
import com.v2ray.ang.core.LauncherManager
import com.v2ray.ang.handler.SettingsManager
import com.v2ray.ang.ui.main.MainAction
import com.v2ray.ang.ui.main.MainRepository
import com.v2ray.ang.ui.main.MainViewModel
import com.v2ray.ang.ui.screen.ActiveServerUi
import com.v2ray.ang.ui.screen.ConfigProtocol
import com.v2ray.ang.ui.screen.ConnectionState
import com.v2ray.ang.ui.screen.HomeScreen
import com.v2ray.ang.ui.screen.ProfileScreen
import com.v2ray.ang.ui.screen.SettingScreen
import com.v2ray.ang.ui.screen.demoAdvancedItems
import com.v2ray.ang.ui.screen.demoAppearanceItems
import com.v2ray.ang.ui.screen.demoConnectionToggles
import com.v2ray.ang.ui.screen.filterByProtocol
import com.v2ray.ang.ui.screen.toConfigItemUi
import com.v2ray.ang.ui.theme.BackgroundDark
import com.v2ray.ang.ui.theme.PrimaryBlue
import com.v2ray.ang.ui.theme.PrimaryBluePale
import com.v2ray.ang.ui.theme.SurfaceDark
import com.v2ray.ang.ui.theme.TextDisabled
import com.v2ray.ang.ui.theme.V2MTheme
import com.v2ray.ang.util.LogUtil
import com.v2ray.ang.util.Utils

/**
 * تب‌های پایین صفحه. Groups فعلاً یه placeholder ساده‌ست چون
 * صفحه‌ی جدا براش نساختیم (می‌تونیم بعداً اضافه کنیم).
 */
private enum class BottomTab(val label: String, val icon: ImageVector) {
    HOME("خانه", Icons.Filled.Home),
    PROFILES("کانفیگ‌ها", Icons.Filled.Layers),
    GROUPS("گروه‌ها", Icons.Filled.Group),
    SETTINGS("تنظیمات", Icons.Filled.Settings)
}

/**
 * اکتیویتی اصلی UI جدید. دکمه‌ی Connect منطق واقعی VpnService.prepare + LauncherManager
 * رو داره. دکمه‌ی سه‌نقطه‌ی صفحه‌ی کانفیگ‌ها الان از کلیپ‌بورد Import می‌کنه (لینک اشتراک
 * یا کانفیگ رو کپی کن، بزن رو سه‌نقطه).
 *
 * TODO باقی‌مونده: پرچم سرور فعال از SpeedtestManager.getRemoteIPInfo، آمار زنده‌ی
 * دانلود/آپلود، دکمه‌ی + جدا برای افزودن دستی، و وصل کردن سوییچ‌های تنظیمات.
 */
class ComposeMainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application, MainRepository(application as AngApplication))
    }

    // دقیقاً همون requestVpnPermission تو MainActivity.kt
    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) startV2Ray()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mainViewModel.onAction(MainAction.Initialize)
        setContent {
            V2MTheme {
                V2MApp(
                    mainViewModel = mainViewModel,
                    onToggleConnection = { handleFabAction() },
                    onImportClipboard = { importClipboard() }
                )
            }
        }
    }

    // دقیقاً همون handleFabAction تو MainActivity.kt
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

    // دقیقاً همون startV2Ray تو MainActivity.kt (بدون بخش مجوز شبکه‌ی محلی که پیچیدگی اضافه داره)
    private fun startV2Ray() {
        if (mainViewModel.uiState.value.selectedGuid.isNullOrEmpty()) {
            toastError("یک کانفیگ رو اول از تب کانفیگ‌ها انتخاب کن")
            return
        }
        LauncherManager.startService(this)
    }

    // دقیقاً همون importClipboard تو MainActivity.kt
    private fun importClipboard() {
        try {
            val text = Utils.getClipboard(this)
            mainViewModel.onAction(MainAction.ImportBatchConfig(text))
        } catch (e: Exception) {
            LogUtil.e(AppConfig.TAG, "Failed to import config from clipboard", e)
        }
    }
}

@Composable
private fun V2MApp(
    mainViewModel: MainViewModel,
    onToggleConnection: () -> Unit,
    onImportClipboard: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }
    var selectedProtocol by remember { mutableStateOf(ConfigProtocol.ALL) }

    val uiState by mainViewModel.uiState.collectAsState()
    val realServers by mainViewModel
        .serversForGroup(uiState.selectedGroupId)
        .collectAsState()

    val filteredConfigs = remember(selectedProtocol, realServers, uiState.selectedGuid) {
        realServers
            .filterByProtocol(selectedProtocol)
            .map { it.toConfigItemUi(isSelected = it.guid == uiState.selectedGuid) }
    }

    // سرور فعال برای کارت بالای صفحه‌ی خانه - بر اساس selectedGuid واقعی
    val activeServerItem = remember(realServers, uiState.selectedGuid) {
        realServers.firstOrNull { it.guid == uiState.selectedGuid }
    }
    val connectionState = if (uiState.isRunning) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED

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
                // TODO: آمار واقعی دانلود/آپلود - هنوز تو MainUiState فیلدی براش نیست
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
                    mainViewModel.onAction(MainAction.SelectServer(config.id))
                },
                onConfigPing = { config ->
                    mainViewModel.onAction(MainAction.SelectServer(config.id))
                    mainViewModel.onAction(MainAction.TestCurrentServer)
                },
                // فعلاً دکمه‌ی + هم همون کار Import از کلیپ‌بورد رو می‌کنه؛
                // بعداً می‌تونیم یه صفحه‌ی جدا برای افزودن دستی/QR بسازیم
                onAddConfig = onImportClipboard,
                onOpenMenu = { /* TODO: باز کردن drawer یا منو */ },
                onSearch = { /* TODO: باز کردن سرچ */ },
                onFilter = { /* TODO: باز کردن فیلتر */ },
                onMoreOptions = onImportClipboard
            )

            BottomTab.GROUPS -> GroupsPlaceholder(modifier = Modifier.padding(padding))

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

/**
 * جایگزین موقت تا صفحه‌ی واقعی گروه‌ها (ServerGroupActivity) رو به Compose
 * وصل کنیم یا یه GroupsScreen.kt جدا بسازیم.
 */
@Composable
private fun GroupsPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Text("صفحه‌ی گروه‌ها هنوز ساخته نشده", color = TextDisabled)
    }
}
