import java.io.Serializable

data class RankPeriod(
    val rank: Int,
    var days: Int // Number of days required to reach the next rank
) : Serializable