package com.example.habitquest.model

data class Habit(
    val id: Int,
    val title: String,
    val xpReward: Int,
    var isCompleted: Boolean = false
)

data class User(
    var name: String,
    var level: Int = 1,
    var currentXp: Int = 0,
    var health: Int = 100
) {
    fun addXp(amount: Int) {
        currentXp += amount
        val xpToNextLevel = level * 100
        if (currentXp >= xpToNextLevel) {
            currentXp -= xpToNextLevel
            level++
        }
    }

    fun takeDamage(amount: Int) {
        health = (health - amount).coerceAtLeast(0)
    }
}
