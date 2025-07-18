package persistance

import Player
import com.google.gson.Gson
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date

val StateFile: File = File("${System.getProperty("user.home")}/wildblood-dev/gtgcounter/state-${LocalDate.now()}.json")

data class SessionState(
    val startTime: Date,
    val startingStats: Player
) {
    companion object {
        fun retrieveTodayStateOrDefault(player: Player): SessionState {
            return try {
                if (StateFile.exists()) {
                    return Gson().fromJson(StateFile.readText(), SessionState::class.java)
                } else {
                    player.save();
                }
            } catch (_: Exception) {
                player.save();
            }
        }
    }
}

fun Player.save(): SessionState {
    val sessionState = SessionState(Date(), this);
    StateFile.parentFile?.mkdirs()
    StateFile.writeText(Gson().toJson(sessionState))
    return sessionState
}
