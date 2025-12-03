package com.example.habitquest

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habitquest.model.Habit
import com.example.habitquest.model.User
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var user: User
    private val habits = mutableListOf<Habit>()
    private lateinit var adapter: HabitAdapter

    private lateinit var tvUserLevel: TextView
    private lateinit var tvUserXp: TextView
    private lateinit var tvUserHealth: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize User
        user = User(name = "Hero")

        // Initialize UI components
        tvUserLevel = findViewById(R.id.tvUserLevel)
        tvUserXp = findViewById(R.id.tvUserXp)
        tvUserHealth = findViewById(R.id.tvUserHealth)
        val btnAddHabit = findViewById<Button>(R.id.btnAddHabit)
        val rvHabits = findViewById<RecyclerView>(R.id.rvHabits)

        // Setup RecyclerView
        adapter = HabitAdapter(habits) { habit, isChecked ->
            onHabitChecked(habit, isChecked)
        }
        rvHabits.layoutManager = LinearLayoutManager(this)
        rvHabits.adapter = adapter

        // Setup Buttons
        btnAddHabit.setOnClickListener {
            addRandomHabit()
        }

        // Add some initial habits
        addHabit("Drink Water", 10)
        addHabit("Read 10 pages", 20)
        addHabit("Exercise", 50)

        updateUserStats()
    }

    private fun addHabit(title: String, xp: Int) {
        val id = if (habits.isEmpty()) 1 else habits.maxOf { it.id } + 1
        habits.add(Habit(id, title, xp))
        adapter.notifyItemInserted(habits.size - 1)
    }

    private fun addRandomHabit() {
        val tasks = listOf("Meditate", "Walk the dog", "Code", "Clean room", "Call mom")
        val randomTask = tasks.random()
        val randomXp = (1..5).random() * 10
        addHabit(randomTask, randomXp)
    }

    private fun onHabitChecked(habit: Habit, isChecked: Boolean) {
        habit.isCompleted = isChecked
        if (isChecked) {
            user.addXp(habit.xpReward)
        } else {
            // Optional: Remove XP if unchecked? For now let's just keep it simple or implement 'health' mechanics?
            // The prompt says "Health is lost if you skip a habit".
            // Here we are just toggling completion.
            // Let's implement a simple penalty for unchecking a completed task just for demo purposes,
            // or just ignore it.
            // If we strictly follow RPG logic, unchecking shouldn't exist easily, but for a tracker it does.
            // Let's assume unchecking is correcting a mistake, so we remove XP.

            // Logic to reverse XP gain (simplified, ignoring level down for now)
            user.currentXp -= habit.xpReward
            if (user.currentXp < 0) user.currentXp = 0
        }
        updateUserStats()
    }

    private fun updateUserStats() {
        tvUserLevel.text = "Level: ${user.level}"
        val xpToNext = user.level * 100
        tvUserXp.text = "XP: ${user.currentXp} / $xpToNext"
        tvUserHealth.text = "Health: ${user.health}"
    }
}
