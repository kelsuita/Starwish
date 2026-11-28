package com.kelsuita.starswish

import com.kelsuita.starswish.data.SavingGoal
import com.kelsuita.starswish.data.reorderGoalSlice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GoalOrderTest {
    private val goals = listOf("active1", "vault1", "active2", "vault2").map { id ->
        SavingGoal(id, id, 100L, emptySet(), archived = id.startsWith("vault"))
    }

    @Test fun vaultSortKeepsHomeOrder() {
        val ordered = reorderGoalSlice(goals, listOf("vault2", "vault1"), archived = true)
        assertEquals(listOf("active1", "vault2", "active2", "vault1"), ordered.map { it.id })
    }

    @Test fun homeSortKeepsVaultOrder() {
        val ordered = reorderGoalSlice(goals, listOf("active2", "active1"), archived = false)
        assertEquals(listOf("active2", "vault1", "active1", "vault2"), ordered.map { it.id })
    }

    @Test fun staleOrderCannotMoveAWishBetweenLists() {
        assertThrows(IllegalArgumentException::class.java) {
            reorderGoalSlice(goals, listOf("vault1", "active2"), archived = true)
        }
    }
}
