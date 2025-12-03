package com.example.habitquest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habitquest.model.Habit

class HabitAdapter(
    private val habits: List<Habit>,
    private val onHabitClick: (Habit, Boolean) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbCompleted: CheckBox = view.findViewById(R.id.cbCompleted)
        val tvHabitTitle: TextView = view.findViewById(R.id.tvHabitTitle)
        val tvXpReward: TextView = view.findViewById(R.id.tvXpReward)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.tvHabitTitle.text = habit.title
        holder.tvXpReward.text = "+${habit.xpReward} XP"

        // Remove listener before setting state to avoid infinite loops or triggering
        holder.cbCompleted.setOnCheckedChangeListener(null)
        holder.cbCompleted.isChecked = habit.isCompleted

        holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            onHabitClick(habit, isChecked)
        }
    }

    override fun getItemCount() = habits.size
}
