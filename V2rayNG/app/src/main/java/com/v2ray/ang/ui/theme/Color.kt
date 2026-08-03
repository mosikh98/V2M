package com.v2ray.ang.ui.theme

import androidx.compose.ui.graphics.Color

// پس‌زمینه
val BackgroundDark = Color(0xFF080B12)
val SurfaceDark = Color(0xFF12161F)
val SurfaceVariantDark = Color(0xFF1A1F2B)

// رنگ اصلی (بنفش/آبی - شبیه دکمه‌ی Connect در طرح)
val PrimaryBlue = Color(0xFF5865F2)
val PrimaryBluePale = Color(0xFF7B87FF)
val AccentCyan = Color(0xFF00E5FF)

// وضعیت‌ها
val SuccessGreen = Color(0xFF00E676)
val WarningYellow = Color(0xFFFFC107)
val ErrorRed = Color(0xFFFF5252)

// متن
val TextPrimary = Color(0xFFF5F6FA)
val TextSecondary = Color(0xFF9AA0B4)
val TextDisabled = Color(0xFF5A6072)

// گرادیانت دکمه‌ی اتصال (از طرح: آبی به بنفش)
val ConnectGradientStart = Color(0xFF00E5FF)
val ConnectGradientEnd = Color(0xFF5865F2)

// رنگ سیگنال بر اساس پینگ (برای استفاده در ServerCard)
fun pingColor(pingMs: Int): Color = when {
    pingMs <= 0 -> TextDisabled          // آفلاین / نامشخص
    pingMs < 100 -> SuccessGreen
    pingMs < 250 -> WarningYellow
    else -> ErrorRed
}
