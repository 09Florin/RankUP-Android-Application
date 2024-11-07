package com.example.rankup

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import Player
import PlayerAdapter
import RankPeriod
import android.widget.Toast
import java.io.File
import java.lang.reflect.Type
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import android.app.DatePickerDialog
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val players = mutableListOf<Player>()

    private var rankPeriods = mutableListOf(
        RankPeriod(1, 14), // Rank 1 to 2
        RankPeriod(2, 21), // Rank 2 to 3
        RankPeriod(3, 40), // Rank 3 to 4
        RankPeriod(4, 50)  // Rank 4 to 5
    )

    private lateinit var playerAdapter: PlayerAdapter

    private lateinit var rank2DaysInput: EditText
    private lateinit var rank3DaysInput: EditText
    private lateinit var rank4DaysInput: EditText
    private lateinit var rank5DaysInput: EditText

    private lateinit var tvCurrentRank2Days: TextView
    private lateinit var tvCurrentRank3Days: TextView
    private lateinit var tvCurrentRank4Days: TextView
    private lateinit var tvCurrentRank5Days: TextView

    private var entryDate: Long = System.currentTimeMillis() // Default to current time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        rank2DaysInput = findViewById(R.id.etRank2Days)
        rank3DaysInput = findViewById(R.id.etRank3Days)
        rank4DaysInput = findViewById(R.id.etRank4Days)
        rank5DaysInput = findViewById(R.id.etRank5Days)

        tvCurrentRank2Days = findViewById(R.id.tvCurrentRank2Days)
        tvCurrentRank3Days = findViewById(R.id.tvCurrentRank3Days)
        tvCurrentRank4Days = findViewById(R.id.tvCurrentRank4Days)
        tvCurrentRank5Days = findViewById(R.id.tvCurrentRank5Days)

        loadPlayerData()
        loadRankPeriodsData()

        playerAdapter = PlayerAdapter(players, rankPeriods) { player ->
            rankUpPlayer(player)
        }

        findViewById<RecyclerView>(R.id.recyclerViewPlayers).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = playerAdapter
        }

        findViewById<Button>(R.id.btnAddPlayer).setOnClickListener {
            showNameInputDialog()
        }

        findViewById<Button>(R.id.btnRemovePlayer).setOnClickListener {
            showRemovePlayerDialog()
        }

        findViewById<Button>(R.id.btnSaveRankPeriods).setOnClickListener {
            updateRankPeriods()
            saveRankPeriodsData() // Save both players and rank periods
            Toast.makeText(this, "Rank-up periods updated and saved!", Toast.LENGTH_SHORT).show()
        }

        displayCurrentRankPeriods()
    }

    private fun displayCurrentRankPeriods() {
        tvCurrentRank2Days.text = "(${rankPeriods[0].days} days)"
        tvCurrentRank3Days.text = "(${rankPeriods[1].days} days)"
        tvCurrentRank4Days.text = "(${rankPeriods[2].days} days)"
        tvCurrentRank5Days.text = "(${rankPeriods[3].days} days)"
    }

    private fun updateRankPeriods() {
        // Check if all current rank day values are zero
        if (tvCurrentRank2Days.text.toString().toIntOrNull() == 0 &&
            tvCurrentRank3Days.text.toString().toIntOrNull() == 0 &&
            tvCurrentRank4Days.text.toString().toIntOrNull() == 0 &&
            tvCurrentRank5Days.text.toString().toIntOrNull() == 0) {

            // Set default values if all are zero
            rankPeriods[0].days = 14  // Default for rank2
            rankPeriods[1].days = 21  // Default for rank3
            rankPeriods[2].days = 40  // Default for rank4
            rankPeriods[3].days = 50  // Default for rank5
        } else {
            // Otherwise, update based on user input
            rankPeriods[0].days = rank2DaysInput.text.toString().toIntOrNull() ?: rankPeriods[0].days
            rankPeriods[1].days = rank3DaysInput.text.toString().toIntOrNull() ?: rankPeriods[1].days
            rankPeriods[2].days = rank4DaysInput.text.toString().toIntOrNull() ?: rankPeriods[2].days
            rankPeriods[3].days = rank5DaysInput.text.toString().toIntOrNull() ?: rankPeriods[3].days
        }

        // Call to update the display with current rank periods
        displayCurrentRankPeriods()
    }

    private fun showNameInputDialog() {
        val nameInput = EditText(this)
        nameInput.hint = "Enter player name"

        AlertDialog.Builder(this)
            .setTitle("Add Player")
            .setView(nameInput)
            .setPositiveButton("OK") { dialog, _ ->
                val playerName = nameInput.text.toString().trim()

                if (playerName.isNotEmpty()) {
                    // Proceed to the date selection dialog
                    showDatePickerDialog(playerName)
                } else {
                    Toast.makeText(this, "Player name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showDatePickerDialog(playerName: String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Set entryDate based on user selection
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            entryDate = selectedCalendar.timeInMillis

            // Now create the player
            createPlayer(playerName, entryDate)

        }, year, month, day).show()
    }

    private fun createPlayer(playerName: String, entryDate: Long) {

        val player = Player(playerName, rank = 1, entryDate = entryDate)
        players.add(player)
        playerAdapter.notifyItemInserted(players.size - 1)

        savePlayerData() // Save player data after addition
        Toast.makeText(this, "Player added: $playerName", Toast.LENGTH_SHORT).show()
    }

    private fun rankUpPlayer(player: Player) {

        if (player.rank < 6) {
            player.rank += 1
            player.lastRankUpDate = System.currentTimeMillis()
            playerAdapter.notifyDataSetChanged()

            savePlayerData() // Save player data after rank up
        }
    }

    private fun showRemovePlayerDialog() {
        val playerNames = players.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select a player to remove")
            .setItems(playerNames) { _, which ->
                players.removeAt(which)
                playerAdapter.notifyDataSetChanged()

                savePlayerData() // Save player data after removal
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun savePlayerData() {
        val gson = Gson()
        val json = gson.toJson(players)
        val file = File(filesDir, "players.json")
        file.writeText(json) // Write JSON data to the file
    }

    private fun loadPlayerData() {
        val file = File(filesDir, "players.json")

        if (file.exists()) {
            val json = file.readText()
            val type: Type = object : TypeToken<MutableList<Player>>() {}.type
            val loadedPlayers: MutableList<Player> = Gson().fromJson(json, type)

            players.clear() // Clear existing players
            players.addAll(loadedPlayers) // Add loaded players
        }
    }

    private fun saveRankPeriodsData() {
        val gson = Gson()
        val json = gson.toJson(rankPeriods)
        val file = File(filesDir, "rankPeriods.json")
        file.writeText(json) // Write JSON data to the file
    }

    private fun loadRankPeriodsData() {
        val file = File(filesDir, "rankPeriods.json")

        if (file.exists()) {
            val json = file.readText()
            val type: Type = object : TypeToken<MutableList<RankPeriod>>() {}.type
            val loadedRankPeriods: MutableList<RankPeriod> = Gson().fromJson(json, type)

            rankPeriods.clear() // Clear existing players
            rankPeriods.addAll(loadedRankPeriods) // Add loaded players

            displayCurrentRankPeriods()  // Update UI after loading
        }
    }
}
