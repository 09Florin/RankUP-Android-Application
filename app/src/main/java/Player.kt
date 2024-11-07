import java.io.Serializable

data class Player(
    val name: String,
    var rank: Int,
    var entryDate: Long, // Timestamp for player entry date
    var lastRankUpDate: Long? = null // Timestamp for last rank-up date
) : Serializable

