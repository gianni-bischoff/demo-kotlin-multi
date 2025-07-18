import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.URI
import java.util.Optional
import kotlin.time.Duration.Companion.minutes
import components.StatComponent
import persistance.SessionState
import tooling.makeWindowClickThrough

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
    var firstStats: Player? by remember { mutableStateOf(null) }
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

    if(playerData != null && firstStats == null) firstStats = SessionState.retrieveTodayStateOrDefault(playerData).startingStats

    firstStats?.let {
        playerData?.sessionKills        = playerData.kills - it.kills
        playerData?.sessionDeaths       = playerData.deaths - it.deaths
        playerData?.sessionDamage       = playerData.damageDealt - it.damageDealt
        playerData?.sessionHours        = playerData.sessionHours - it.sessionHours
        playerData?.sessionHeadshots    = playerData.headshots - it.headshots
    }


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
            playerData != null -> StatComponent(playerData)
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
        this.window.makeWindowClickThrough()

        app()
    }
}


