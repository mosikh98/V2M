package com.v2ray.ang.ui.screen

import com.v2ray.ang.dto.entities.ServersCache

/**
 * الگوی تشخیص ایموجی پرچم در ابتدای یه رشته (مثلاً remark کانفیگ).
 * پرچم‌های کشور تو یونیکد به‌صورت یه جفت "Regional Indicator Symbol" ساخته میشن.
 * چون خودِ v2rayNG برای هر کانفیگ پرچم تشخیص نمی‌ده، این تابع فقط سعی می‌کنه
 * پرچمی که از قبل تو اسم کانفیگ (توسط اشتراک یا خود کاربر) گذاشته شده رو پیدا کنه.
 */
private val leadingFlagEmojiRegex = Regex("^[\uD83C][\uDDE6-\uDDFF][\uD83C][\uDDE6-\uDDFF]")

private fun extractLeadingFlagEmoji(text: String): String? =
    leadingFlagEmojiRegex.find(text.trim())?.value

/**
 * برچسب پروتکل برای نمایش تو کارت (مثلاً "vless · ws · tls").
 */
private fun ServersCache.protocolLabel(): String {
    val parts = listOfNotNull(
        profile.configType.name.lowercase(),
        profile.network?.takeIf { it.isNotBlank() },
        profile.security?.takeIf { it.isNotBlank() }
    )
    return parts.joinToString(" · ")
}

/**
 * تبدیل یک ServersCache واقعی (از MainViewModel) به مدل UI صفحه‌ی کانفیگ‌ها.
 * اگه remark با یه ایموجی پرچم شروع بشه همون رو نشون می‌ده، وگرنه یه آیکون ژنریک.
 */
fun ServersCache.toConfigItemUi(isSelected: Boolean): ConfigItemUi {
    val remark = profile.remarks.ifBlank { profile.server ?: guid }
    return ConfigItemUi(
        id = guid,
        countryFlag = extractLeadingFlagEmoji(remark) ?: "🌐",
        name = remark,
        protocolLabel = protocolLabel(),
        pingMs = testDelayMillis.toInt(),
        isSelected = isSelected
    )
}

/**
 * فیلتر لیست بر اساس تب پروتکل انتخاب‌شده (همه/VLESS/VMESS/Shadowsocks).
 */
fun List<ServersCache>.filterByProtocol(protocol: ConfigProtocol): List<ServersCache> =
    when (protocol) {
        ConfigProtocol.ALL -> this
        ConfigProtocol.VLESS -> filter { it.profile.configType.name == "VLESS" }
        ConfigProtocol.VMESS -> filter { it.profile.configType.name == "VMESS" }
        ConfigProtocol.SHADOWSOCKS -> filter { it.profile.configType.name == "SHADOWSOCKS" }
    }
