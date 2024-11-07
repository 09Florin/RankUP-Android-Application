import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rankup.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlayerAdapter(
    private val players: List<Player>,
    private val rankPeriods: List<RankPeriod>,
    private val onRankUp: (Player) -> Unit
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        holder.bind(player)
    }

    override fun getItemCount(): Int = players.size

    inner class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPlayerName: TextView = itemView.findViewById(R.id.tvPlayerName)
        private val tvPlayerRank: TextView = itemView.findViewById(R.id.tvPlayerRank)
        private val tvEntryDate: TextView = itemView.findViewById(R.id.tvEntryDate)
        private val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)
        private val btnRankUp: Button = itemView.findViewById(R.id.btnRankUp)

        fun bind(player: Player) {
            tvPlayerName.text = player.name
            tvPlayerRank.text = "Rank ${player.rank}"  // Display the player's rank
            tvEntryDate.text =
                SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(player.entryDate))

            // Determine the required rank period for the current player's rank
            val rankPeriod = rankPeriods.firstOrNull { it.rank == player.rank }
            val requiredDaysForNextRank = rankPeriod?.days ?: 0

            // Calculate the difference in days between the current date and the player's entry date
            val currentTimeMillis = System.currentTimeMillis()
            val differenceInDays = (currentTimeMillis - player.entryDate) / (1000 * 60 * 60 * 24)

            // Determine eligibility for rank-up based on the difference in days
            val eligibleForRankUp = differenceInDays >= requiredDaysForNextRank

            // Set imgStatus visibility based on eligibility
            imgStatus.visibility = if (eligibleForRankUp && player.rank < 5) View.VISIBLE else View.GONE

            // Hide rank-up button for max rank players
            btnRankUp.visibility = if (player.rank >= 6) View.GONE else View.VISIBLE

            // Rank-up button click handling
            btnRankUp.setOnClickListener {
                onRankUp(player)
                notifyItemChanged(adapterPosition)
            }
        }
    }
}
