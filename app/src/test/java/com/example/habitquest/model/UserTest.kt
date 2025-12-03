package com.example.habitquest.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserTest {

    @Test
    fun userStartsWithLevel1AndZeroXp() {
        val user = User(name = "Test User")
        assertEquals(1, user.level)
        assertEquals(0, user.currentXp)
    }

    @Test
    fun addingXpLevelsUpUser() {
        val user = User(name = "Test User")
        // Level 1 requires 100 XP
        user.addXp(100)
        assertEquals(2, user.level)
        assertEquals(0, user.currentXp)
    }

    @Test
    fun xpCarriesOverOnLevelUp() {
        val user = User(name = "Test User")
        // Level 1 requires 100 XP. Adding 120.
        user.addXp(120)
        assertEquals(2, user.level)
        assertEquals(20, user.currentXp)
    }

    @Test
    fun takingDamageReducesHealth() {
        val user = User(name = "Test User")
        user.takeDamage(10)
        assertEquals(90, user.health)
    }

    @Test
    fun healthDoesNotGoBelowZero() {
        val user = User(name = "Test User")
        user.takeDamage(150)
        assertEquals(0, user.health)
    }
}
