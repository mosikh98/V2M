package com.v2ray.ang.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.ui.theme.*

/**
 * پروتکل کانفیگ - برای تب‌بندی و فیلتر بالای صفحه.
 */
enum class ConfigProtocol(val label: String) {
    ALL("همه"),
    VLESS("VLESS"),
    VMESS("VMESS"),
    SHADOWSOCKS("Shadowsocks")
}

/**
 * مدل نمایشی یک کانفیگ تو لیست.
 */
data class ConfigItemUi(
    val id: String,
    val countryFlag: String,
    val name: String,
    val protocolLabel: String,
    val pingMs: Int,
    val isSelected: Boolean = false
)

/**
 * یک آیتم تو منوی کشویی سه‌نقطه (مثلاً "Import از کلیپ‌بورد").
 */
data class MoreMenuItem(
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun ProfileScreen(
    configs: List<ConfigItemUi>,
    selectedProtocol: ConfigProtocol,
    onProtocolSelected: (ConfigProtocol) -> Unit,
    onConfigClick: (ConfigItemUi) -> Unit,
    onConfigPing: (ConfigItemUi) -> Unit,
    onAddConfig: () -> Unit,
    onOpenMenu: () -> Unit,
    onSearch: () -> Unit,
    onFilter: () -> Unit,
    moreMenuItems: List<MoreMenuItem>,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            ProfileTopBar(
                onOpenMenu = onOpenMenu,
                onSearch = onSearch,
                onFilter = onFilter,
                moreMenuItems = moreMenuItems
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddConfig,
                containerColor = PrimaryBlue,
                contentColor = TextPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن کانفیگ")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(padding)
        ) {
            ProtocolTabs(selectedProtocol, onProtocolSelected)

            Spacer(Modifier.height(8.dp))

            if (configs.isEmpty()) {
                EmptyConfigState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(configs, key = { it.id }) { config ->
                        ConfigCard(
                            config = config,
                            onClick = { onConfigClick(config) },
                            onPingClick = { onConfigPing(config) }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) } // فضا برای FAB
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(
    onOpenMenu: () -> Unit,
    onSearch: () -> Unit,
    onFilter: () -> Unit,
    moreMenuItems: List<MoreMenuItem>
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("کانفیگ‌ها", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            IconButton(onClick = onOpenMenu) {
                Icon(Icons.Filled.Menu, contentDescription = "منو", tint = TextPrimary)
            }
        },
        actions = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, contentDescription = "جستجو", tint = TextPrimary)
            }
            IconButton(onClick = onFilter) {
                Icon(Icons.Filled.FilterList, contentDescription = "فیلتر", tint = TextPrimary)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "بیشتر", tint = TextPrimary)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = SurfaceDark
                ) {
                    moreMenuItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.label, color = TextPrimary) },
                            onClick = {
                                menuExpanded = false
                                item.onClick()
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BackgroundDark,
            titleContentColor = TextPrimary
        )
    )
}

@Composable
private fun ProtocolTabs(
    selected: ConfigProtocol,
    onSelected: (ConfigProtocol) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ConfigProtocol.values().forEach { protocol ->
            val isSelected = protocol == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) PrimaryBlue else SurfaceDark)
                    .clickable { onSelected(protocol) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = protocol.label,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ConfigCard(
    config: ConfigItemUi,
    onClick: () -> Unit,
    onPingClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (config.isSelected) SurfaceVariantDark else SurfaceDark)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = config.countryFlag, fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    config.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    config.protocolLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryBluePale
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onPingClick)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(pingColor(config.pingMs))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (config.pingMs > 0) "${config.pingMs} ms" else "—",
                style = MaterialTheme.typography.bodyMedium,
                color = pingColor(config.pingMs)
            )
        }
    }
}

@Composable
private fun EmptyConfigState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "هنوز کانفیگی اضافه نکردی",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "با دکمه‌ی + یه کانفیگ یا اشتراک اضافه کن",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDisabled
        )
    }
}
