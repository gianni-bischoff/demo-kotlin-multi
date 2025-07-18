package components

import Player
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatComponent(playerData: Player): Unit = Text(
    text = """
        |Kills: ${playerData.kills} | ${playerData.sessionKills}
        |Deaths: ${playerData.deaths} | ${playerData.sessionDeaths}
        |Headshots: ${playerData.headshots} | ${playerData.sessionHeadshots}
    """.trimMargin(),
    color = Color.Green,
    modifier = Modifier.padding(top = 20.dp, end = 10.dp)
)