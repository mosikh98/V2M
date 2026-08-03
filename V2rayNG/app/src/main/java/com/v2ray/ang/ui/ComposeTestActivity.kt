package com.v2ray.ang.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.v2ray.ang.ui.screen.ActiveServerUi
import com.v2ray.ang.ui.screen.ConnectionState
import com.v2ray.ang.ui.screen.HomeScreen
import com.v2ray.ang.ui.theme.V2MTheme

/**
 * اکتیویتی موقت فقط برای تست HomeScreen با دیتای فرضی (dummy data).
 * وقتی مطمئن شدیم UI درست کار می‌کنه، این رو با MainActivity واقعی
 * یکی می‌کنیم یا دیتای واقعی رو بهش وصل می‌کنیم.
 *
 * برای اجرا: این Activity رو موقتاً به عنوان LAUNCHER تو AndroidManifest.xml معرفی کن
 * (یا از یه دکمه‌ی موقت تو MainActivity فعلی صداش بزن) تا بتونی تستش کنی.
 */
class ComposeTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            V2MTheme {
                // TODO(قدم بعدی): این‌ها رو با دیتای واقعی از ProfileManager / MmkvManager عوض کن
                var connectionState by remember {
                    mutableStateOf(ConnectionState.DISCONNECTED)
                }

                HomeScreen(
                    connectionState = connectionState,
                    activeServer = ActiveServerUi(
                        countryFlag = "🇩🇪",
                        name = "Germany - Frankfurt",
                        protocol = "vless · ws · tls",
                        pingMs = 98
                    ),
                    downloadBytes = "126.4 MB",
                    uploadBytes = "23.7 MB",
                    profileCount = 12,
                    groupCount = 3,
                    onConnectToggle = {
                        connectionState = when (connectionState) {
                            ConnectionState.DISCONNECTED -> ConnectionState.CONNECTING
                            ConnectionState.CONNECTING -> ConnectionState.CONNECTED
                            ConnectionState.CONNECTED -> ConnectionState.DISCONNECTED
                        }
                        // TODO: اینجا باید VpnService واقعی رو start/stop کنی
                    },
                    onOpenProfiles = { /* TODO: باز کردن صفحه‌ی پروفایل‌ها */ },
                    onOpenGroups = { /* TODO: باز کردن صفحه‌ی گروه‌ها */ },
                    onOpenSettings = { /* TODO: باز کردن تنظیمات */ },
                    onOpenLogs = { /* TODO: باز کردن لاگ‌ها */ }
                )
            }
        }
    }
}
