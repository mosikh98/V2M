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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.v2ray.ang.ui.theme.*

/**
 * مدل نمایشی یک گروه/اشتراک تو لیست.
 */
data class GroupItemUi(
    val id: String,
    val remarks: String,
    val isSelected: Boolean
)

@Composable
fun GroupsScreen(
    groups: List<GroupItemUi>,
    onGroupClick: (GroupItemUi) -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = { GroupsTopBar(onOpenMenu) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(padding)
        ) {
            if (groups.isEmpty()) {
                EmptyGroupsState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(groups, key = { it.id.ifBlank { "default" } }) { group ->
                        GroupCard(group = group, onClick = { onGroupClick(group) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupsTopBar(onOpenMenu: () -> Unit) {
    TopAppBar(
        title = { Text("گروه‌ها", style = MaterialTheme.typography.titleLarge) },
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
private fun GroupCard(group: GroupItemUi, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (group.isSelected) SurfaceVariantDark else SurfaceDark)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Group,
                    contentDescription = null,
                    tint = PrimaryBluePale,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                group.remarks.ifBlank { "پیش‌فرض" },
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextDisabled)
    }
}

@Composable
private fun EmptyGroupsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "هیچ گروهی وجود نداره",
            style = MaterialTheme.typography.titleMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "گروه‌ها با افزودن اشتراک ساخته میشن",
            style = MaterialTheme.typography.bodyMedium,
            color = TextDisabled
        )
    }
}
