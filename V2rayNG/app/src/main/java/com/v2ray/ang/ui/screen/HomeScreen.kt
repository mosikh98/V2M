package com.v2ray.ang.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.ui.theme.*

/**
 * وضعیت اتصال - این چیزیه که باید از VpnService / V2RayServiceManager واقعی بیاد.
 * فعلاً یه enum ساده برای دمو گذاشتم؛ در قدم بعد وصلش می‌کنیم به سرویس واقعی.
 */
enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

/**
 * مدل داده‌ی سرور فعلی برای نمایش در کارت بالای صفحه.
 * این باید از ProfileManager.getSelectedProfile() یا معادلش پر بشه.
 */
data class ActiveServerUi(
    val countryFlag: String,
    val name: String,
    val protocol: String,
    val pingMs: Int
)

@Composable
fun HomeScreen(
    connectionState: ConnectionState,
    activeServer: ActiveServerUi?,
    downloadBytes: String,
    uploadBytes: String,
    profileCount: Int,
    groupCount: Int,
    onConnectToggle: () -> Unit,
    onOpenProfiles: () -> Unit,
    onOpenGroups: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        // --- Header ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "V2M",
                style = MaterialTheme.typography.titleLarge,
                color = PrimaryBluePale
            )
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "تنظیمات", tint = TextSecondary)
            }
        }

        Spacer(Modifier.height(8.dp))
        ConnectionStatusPill(connectionState)

        Spacer(Modifier.height(24.dp))

        // --- دکمه‌ی اتصال ---
        ConnectButton(
            state = connectionState,
            onClick = onConnectToggle,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(12.dp))
        Text(
            text = when (connectionState) {
                ConnectionState.CONNECTED -> "اتصال شما امن و پایدار است"
                ConnectionState.CONNECTING -> "در حال اتصال..."
                ConnectionState.DISCONNECTED -> "برای اتصال ضربه بزنید"
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        // --- کارت سرور فعلی ---
        if (activeServer != null) {
            ActiveServerCard(activeServer, onClick = onOpenProfiles)
            Spacer(Modifier.height(16.dp))
        }

        // --- کارت‌های دانلود/آپلود ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TrafficCard(
                label = "دانلود",
                value = downloadBytes,
                color = SuccessGreen,
                isUpload = false,
                modifier = Modifier.weight(1f)
            )
            TrafficCard(
                label = "آپلود",
                value = uploadBytes,
                color = AccentCyan,
                isUpload = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- شمارنده‌ها ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(
                icon = Icons.Filled.Layers,
                label = "کانفیگ‌ها",
                value = profileCount.toString(),
                onClick = onOpenProfiles,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.Group,
                label = "گروه‌ها",
                value = groupCount.toString(),
                onClick = onOpenGroups,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(
                icon = Icons.Filled.Settings,
                label = "تنظیمات",
                value = "تنظیمات برنامه",
                isTextValue = true,
                onClick = onOpenSettings,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.List,
                label = "لاگ‌ها",
                value = "مشاهده لاگ‌ها",
                isTextValue = true,
                onClick = onOpenLogs,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ConnectionStatusPill(state: ConnectionState) {
    val (bg, dotColor, text) = when (state) {
        ConnectionState.CONNECTED -> Triple(SurfaceVariantDark, SuccessGreen, "متصل")
        ConnectionState.CONNECTING -> Triple(SurfaceVariantDark, WarningYellow, "در حال اتصال")
        ConnectionState.DISCONNECTED -> Triple(SurfaceVariantDark, TextDisabled, "قطع")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

/**
 * دکمه‌ی دایره‌ای گرادیانتی وسط صفحه.
 * onClick باید به VpnService.start()/stop() یا معادل واقعی وصل بشه.
 */
@Composable
fun ConnectButton(
    state: ConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "connect-rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val ringBrush = Brush.sweepGradient(
        listOf(ConnectGradientStart, ConnectGradientEnd, ConnectGradientStart)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(180.dp)
            .clickable(onClick = onClick)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(if (state == ConnectionState.CONNECTING) rotation else 0f)
        ) {
            drawCircle(
                brush = ringBrush,
                radius = size.minDimension / 2,
                center = Offset(size.width / 2, size.height / 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
            )
        }
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PowerSettingsNew,
                contentDescription = "اتصال",
                tint = if (state == ConnectionState.CONNECTED) SuccessGreen else TextPrimary,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@Composable
private fun ActiveServerCard(server: ActiveServerUi, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = server.countryFlag, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(server.name, style = MaterialTheme.typography.titleMedium)
                Text(server.protocol, style = MaterialTheme.typography.labelSmall)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${server.pingMs} ms",
                style = MaterialTheme.typography.bodyMedium,
                color = pingColor(server.pingMs),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TrafficCard(
    label: String,
    value: String,
    color: Color,
    isUpload: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(4.dp))
            Text(if (isUpload) "↑" else "↓", color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = color)
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTextValue: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Icon(icon, contentDescription = label, tint = PrimaryBluePale, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(
                value,
                style = if (isTextValue) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium
            )
        }
    }
}
