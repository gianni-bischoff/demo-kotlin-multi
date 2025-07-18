import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.google.gson.Gson
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef.HWND
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL
import java.util.Optional
import kotlin.math.abs
import kotlin.time.Duration.Companion.minutes
import androidx.compose.ui.input.key.*

data class Player(
    val name: String,
    val guid: String,
    val kills: Int,
    var sessionKills: Int,
    var sessionDeaths: Int,
    var sessionHeadshots: Int,
    var sessionDamage: Double,
    var sessionHours: Double,
    val damageDealt: Double,
    val deaths: Int,
    val playtimeHours: Double,
    val headshots: Int,
    val favoriteWeapon: String
)

data class PvpData(
    val players: List<Player>
)

data class StatsResponse(
    val pvp: PvpData
)

private const val STATS_API_URL = "https://gtg-arma.de/api/stats"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun app() {
    var firstStats: Optional<Player> by remember { mutableStateOf(Optional.empty()) }
    var playerStats by remember { mutableStateOf<List<Player>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val stats = fetchStats()
                playerStats = stats.pvp.players
                isLoading = false
            } catch (e: Exception) {
                println(e)
            }

            delay(5.minutes.inWholeMilliseconds)
        }
    }

    // Find a specific player - modify this to find your player of interest
    val playerName = "Gianni" // Change this to the player you want to track
    val playerData = playerStats.find { it.name == playerName }

    if(firstStats.isEmpty) firstStats = Optional.ofNullable(playerData)

    playerData?.sessionKills        = abs(firstStats.get().kills - playerData.kills)
    playerData?.sessionDeaths       = abs(firstStats.get().deaths - playerData.deaths)
    playerData?.sessionDamage       = abs(firstStats.get().damageDealt - playerData.damageDealt)
    playerData?.sessionHours        = abs(firstStats.get().playtimeHours - playerData.playtimeHours)
    playerData?.sessionHeadshots    = abs(firstStats.get().headshots - playerData.headshots)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0.9f, 0.9f, 0.9f, 0.0f)),
        propagateMinConstraints = true,
    ) {
        Spacer(modifier = Modifier.size(20.dp, 100.dp).fillMaxWidth())
        when {
            isLoading -> Text("Loading stats...")
            errorMessage != null -> Text(errorMessage!!)
            playerData != null -> Text(
                text = """
                    |Kills: ${playerData.kills} | ${playerData.sessionKills}
                    |Deaths: ${playerData.deaths} | ${playerData.sessionDeaths}
                    |Headshots: ${playerData.headshots} | ${playerData.sessionHeadshots}
                """.trimMargin(),
                color = Color.Green,
                modifier = Modifier.padding(top = 20.dp, end = 10.dp)
            )
            else -> Text("Player not found", modifier = Modifier.padding(top = 5.dp, end = 10.dp))
        }
    }
}

suspend fun fetchStats(): StatsResponse {
    return withContext(Dispatchers.IO) {
        val url = URI(STATS_API_URL).toURL()
        val jsonString = url.readText()
        Gson().fromJson(jsonString, StatsResponse::class.java)
    }
}


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            placement = WindowPlacement.Floating,
            position = WindowPosition(alignment = Alignment.TopEnd),
            size = DpSize(120.dp, 300.dp)
        ),
        transparent = true,
        alwaysOnTop = true,
        resizable = false,
        undecorated = true,
        focusable = false,
        title = "GtGKillCounter",
    ) {
        // Apply click-through behavior to the window
        val composeWindow = this.window
        makeWindowClickThrough(composeWindow)

        app()
    }
}

private fun makeWindowClickThrough(window: ComposeWindow) {
    window.background = java.awt.Color(0, 0, 0, 0)

    // On Windows, this makes the window click-through
    com.sun.jna.Platform.isWindows().let {
        try {
            val WIN32_GWL_EXSTYLE = -20
            val WS_EX_LAYERED = 0x00080000
            val WS_EX_TRANSPARENT = 0x00000020

            val hwnd = HWND(Native.getWindowPointer(window))
            val windowsUser32 = com.sun.jna.platform.win32.User32.INSTANCE

            val currentStyle = windowsUser32.GetWindowLong(hwnd, WIN32_GWL_EXSTYLE)
            windowsUser32.SetWindowLong(hwnd, WIN32_GWL_EXSTYLE,
                currentStyle.or(WS_EX_LAYERED).or(WS_EX_TRANSPARENT))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
