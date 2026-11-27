package com.kelsuita.starswish

import com.kelsuita.starswish.data.*
import org.junit.Assert.*
import org.junit.Test

class GoalLinksTest {
    private val account = BankAccount("account", "现金", 250000, 1)
    private val other = BankAccount("other", "招商银行", 100000, 1)

    private fun balanceGoal() = SavingGoal("g1", "旅行", 500000, setOf("other"), GoalMode.BALANCE)
    private fun growthGoal() = SavingGoal("g2", "相机", 800000, setOf("other"), GoalMode.GROWTH,
        baseline = 100000, links = listOf(GoalAccountLink("other", GoalMode.GROWTH, 100000, 5)))

    @Test fun linkingACurrentBalanceWishJustAddsTheAccount() {
        val goal = balanceGoal()
        val updated = linkAccountToGoals(account, listOf(goal), setOf(goal.id), now = 99)
        assertEquals(setOf("other", "account"), updated.single().accountIds)
        assertTrue(updated.single().links.isEmpty())
        assertEquals(0L, updated.single().baseline)
    }

    @Test fun linkingAStartFromNowWishSnapshotsTheBalanceItJoinsWith() {
        val goal = growthGoal()
        val updated = linkAccountToGoals(account, listOf(goal), setOf(goal.id), now = 99).single()
        assertEquals(setOf("other", "account"), updated.accountIds)
        val added = updated.links.single { it.accountId == "account" }
        assertEquals(250000L, added.baseline)
        assertEquals(99L, added.baselineAt)
        assertEquals(GoalMode.GROWTH, added.mode)
        assertEquals(350000L, updated.baseline)
    }

    @Test fun unlinkingDropsTheSnapshotItBrought() {
        val goal = growthGoal().copy(
            accountIds = setOf("other", "account"),
            links = growthGoal().links + GoalAccountLink("account", GoalMode.GROWTH, 250000, 9),
            baseline = 350000,
        )
        val updated = linkAccountToGoals(account, listOf(goal), emptySet()).single()
        assertEquals(setOf("other"), updated.accountIds)
        assertEquals(listOf("other"), updated.links.map { it.accountId })
        assertEquals(100000L, updated.baseline)
    }

    @Test fun unchangedSelectionsProduceNoUpdates() {
        val linked = growthGoal().copy(accountIds = setOf("other", "account"),
            links = growthGoal().links + GoalAccountLink("account", GoalMode.GROWTH, 250000, 9))
        assertTrue(linkAccountToGoals(account, listOf(linked), setOf(linked.id)).isEmpty())
        val unlinked = balanceGoal()
        assertTrue(linkAccountToGoals(account, listOf(unlinked), emptySet()).isEmpty())
    }
}
