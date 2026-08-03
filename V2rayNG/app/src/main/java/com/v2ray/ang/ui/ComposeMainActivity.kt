package com.v2ray.ang.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
 * اکتیویتی اصلی UI جدید. صفحه‌ی کانفیگ‌ها (Profiles) الان به MainViewModel
 * واقعی وصله - دقیقاً همون معماری‌ای که MainActivity قدیمی استفاده می‌کنه.
 * Home و Settings هنوز با دیتای دمو کار می‌کنن؛ قدم بعدی وصل کردن اوناست.
 */
class ComposeMainActivity : ComponentActivity() {

    // دقیقاً همون الگوی MainActivity.kt - بدون Hilt، ساخت دستی با Factory
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(application, MainRepository(application as AngApplication))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // دقیقاً همون کاری که MainActivity.onCreate می‌کنه
        mainViewModel.onAction(MainAction.Initialize)
        setContent {
            V2MTheme {
                V2MApp(mainViewModel = mainViewModel)
            }
        }
    }
}

@Composable
private fun V2MApp(mainViewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

    // TODO: این دو تا (connectionState دمو و demoConfigs) موقع وصل کردن Home
    // به همین mainViewModel حذف میشن و جاش از uiState واقعی استفاده میشه.
    var connectionState by remember { mutableStateOf(ConnectionState.DISCONNECTED) }
    var selectedProtocol by remember { mutableStateOf(ConfigProtocol.ALL) }

    // ---- دیتای واقعی صفحه‌ی کانفیگ‌ها ----
    val uiState by mainViewModel.uiState.collectAsState()
    val realServers by mainViewModel
        .serversForGroup(uiState.selectedGroupId)
        .collectAsState()

    val filteredConfigs = remember(selectedProtocol, realServers, uiState.selectedGuid) {
        realServers
            .filterByProtocol(selectedProtocol)
            .map { it.toConfigItemUi(isSelected = it.guid == uiState.selectedGuid) }
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
                activeServer = ActiveServerUi(
                    countryFlag = "🇩🇪",
                    name = "Germany - Frankfurt",
                    protocol = "vless · ws · tls",
                    pingMs = 98
                ),
                downloadBytes = "126.4 MB",
                uploadBytes = "23.7 MB",
                profileCount = realServers.size,
                groupCount = uiState.groups.size,
                onConnectToggle = {
                    // TODO: قدم بعدی این رو به mainViewModel.onAction(MainAction.ToggleService) وصل کن
                    connectionState = when (connectionState) {
                        ConnectionState.DISCONNECTED -> ConnectionState.CONNECTING
                        ConnectionState.CONNECTING -> ConnectionState.CONNECTED
                        ConnectionState.CONNECTED -> ConnectionState.DISCONNECTED
                    }
                },
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
                onAddConfig = { /* TODO: باز کردن صفحه‌ی افزودن کانفیگ */ },
                onOpenMenu = { /* TODO: باز کردن drawer یا منو */ },
                onSearch = { /* TODO: باز کردن سرچ */ },
                onFilter = { /* TODO: باز کردن فیلتر */ },
                onMoreOptions = { /* TODO: منوی گزینه‌های بیشتر */ }
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
