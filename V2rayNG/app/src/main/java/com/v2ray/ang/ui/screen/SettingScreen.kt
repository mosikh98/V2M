package com.v2ray.ang.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.v2ray.ang.ui.theme.*

/**
 * یک آیتم سوییچ‌دار (روشن/خاموش) تو تنظیمات.
 * checked باید از MmkvManager یا SharedPreferences واقعی خونده بشه.
 */
data class SettingToggleUi(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val checked: Boolean
)

/**
 * یک آیتم navigable (که با کلیک به یه صفحه‌ی دیگه میره).
 */
data class SettingNavUi(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String
)

@Composable
fun SettingScreen(
    connectionToggles: List<SettingToggleUi>,
    appearanceItems: List<SettingNavUi>,
    advancedItems: List<SettingNavUi>,
    onToggleChanged: (String, Boolean) -> Unit,
    onNavItemClick: (String) -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = { SettingsTopBar(onOpenMenu) }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item { SectionHeader("اتصال") }
            items(connectionToggles, key = { it.id }) { item ->
                ToggleRow(item = item, onCheckedChange = { checked -> onToggleChanged(item.id, checked) })
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { SectionHeader("ظاهر") }
            items(appearanceItems, key = { it.id }) { item ->
                NavRow(item = item, onClick = { onNavItemClick(item.id) })
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { SectionHeader("پیشرفته") }
            items(advancedItems, key = { it.id }) { item ->
                NavRow(item = item, onClick = { onNavItemClick(item.id) })
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onOpenMenu: () -> Unit) {
    TopAppBar(
        title = { Text("تنظیمات", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onOpenMenu) {
                Icon(Icons.Filled.Menu, contentDescription = "منو", tint = TextPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BackgroundDark,
            titleContentColor = TextPrimary
        )
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = PrimaryBluePale,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ToggleRow(item: SettingToggleUi, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            IconBubble(item.icon)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(item.subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
        Switch(
            checked = item.checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = PrimaryBlue,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceVariantDark
            )
        )
    }
}

@Composable
private fun NavRow(item: SettingNavUi, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(item.icon)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(item.subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextDisabled
        )
    }
}

@Composable
private fun IconBubble(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(SurfaceVariantDark),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryBluePale, modifier = Modifier.size(18.dp))
    }
}

/**
 * دیتای فرضی برای پیش‌نمایش/تست — دقیقاً مطابق طرحی که فرستاده بودی.
 * موقع وصل کردن به دیتای واقعی این تابع حذف میشه.
 */
fun demoConnectionToggles(): List<SettingToggleUi> = listOf(
    SettingToggleUi("auto_connect", Icons.Filled.PlayArrow, "اتصال خودکار", "اتصال خودکار هنگام اجرای برنامه", true),
    SettingToggleUi("background_connect", Icons.Filled.Sync, "اتصال در پس‌زمینه", "حفظ اتصال در پس‌زمینه", true),
    SettingToggleUi("vpn_service", Icons.Filled.VpnKey, "سرویس VPN", "استفاده از سرویس VPN", true)
)

fun demoAppearanceItems(): List<SettingNavUi> = listOf(
    SettingNavUi("theme", Icons.Filled.NightsStay, "تم برنامه", "تاریک"),
    SettingNavUi("accent_color", Icons.Filled.Palette, "رنگ اصلی", "آبی بنفش"),
    SettingNavUi("language", Icons.Filled.Language, "زبان", "فارسی")
)

fun demoAdvancedItems(): List<SettingNavUi> = listOf(
    SettingNavUi("routing", Icons.Filled.Route, "مسیر هوشمند (Routing)", "تنظیم مسیر برای سایت‌ها و اپلیکیشن‌ها"),
    SettingNavUi("dns", Icons.Filled.Dns, "DNS", "تنظیم DNS و ضد فیلتر"),
    SettingNavUi("core_settings", Icons.Filled.Tune, "تنظیمات هسته", "تنظیمات پیشرفته Xray")
)
