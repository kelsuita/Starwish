package com.kelsuita.starswish

import com.kelsuita.starswish.data.*
import org.junit.Assert.*
import org.junit.Test

class BannerAggregateTest {

    private val a = BankAccount("a", "招商储蓄", 100_000, 1)
    private val b = BankAccount("b", "工商储蓄", 50_000, 1)

    private fun growth(id: String, accounts: Set<String>, links: List<GoalAccountLink>, target: Long = 100_000) =
        SavingGoal(id, id, target, accounts, GoalMode.GROWTH, links.sumOf { it.baseline }, links = links)

    @Test fun currentBalanceWishSwallowsSharedAccount() {
        val normal = SavingGoal("normal", "旅行", 100_000, setOf("a"))
        val now = growth("now", setOf("a"), listOf(GoalAccountLink("a", GoalMode.GROWTH, 20_000, 1)), target = 50_000)
        // [0,100000] ∪ [20000,100000] is one interval, so the account counts once, not twice.
        assertEquals(100_000L, BannerAggregate.saved(listOf(normal, now), listOf(a)))
        val result = BannerAggregate.result(listOf(normal, now), listOf(a))
        assertEquals(150_000L, result.target)
        assertEquals(2f / 3f, result.progress, 0.0001f)
    }

    @Test fun multipleStartFromNowWishesUseEarliestBaseline() {
        val first = growth("first", setOf("a"), listOf(GoalAccountLink("a", GoalMode.GROWTH, 20_000, 1)))
        val second = growth("second", setOf("a"), listOf(GoalAccountLink("a", GoalMode.GROWTH, 60_000, 1)))
        // Union of [20000,100000] and [60000,100000] is 80000, not 80000 + 40000.
        assertEquals(80_000L, BannerAggregate.saved(listOf(first, second), listOf(a)))
        assertEquals(80_000L, first.saved(listOf(a)))
        assertEquals(40_000L, second.saved(listOf(a)))
    }

    @Test fun balanceBelowBaselineContributesNothing() {
        val goal = growth("goal", setOf("a"), listOf(GoalAccountLink("a", GoalMode.GROWTH, 80_000, 1)))
        val lower = a.copy(balance = 50_000)
        assertEquals(0L, BannerAggregate.saved(listOf(goal), listOf(lower)))
        assertEquals(0L, goal.saved(listOf(lower)))
        assertEquals(0f, BannerAggregate.result(listOf(goal), listOf(lower)).progress, 0f)
    }

    @Test fun multipleAccountsSumAndCapAtTotalTarget() {
        val left = SavingGoal("left", "左", 20_000, setOf("a"))
        val right = SavingGoal("right", "右", 20_000, setOf("b"))
        val capped = BannerAggregate.result(listOf(left, right), listOf(a, b))
        assertEquals(40_000L, capped.target)
        assertEquals(40_000L, capped.saved)
        assertEquals(1f, capped.progress, 0f)
        val roomy = BannerAggregate.result(listOf(left.copy(target = 100_000), right.copy(target = 100_000)), listOf(a, b))
        assertEquals(150_000L, roomy.saved)
        assertEquals(0.75f, roomy.progress, 0.0001f)
    }

    @Test fun perAccountSnapshotClampsEachAccountIndependently() {
        val goal = SavingGoal("goal", "应急金", 500_000, setOf("a", "b"), GoalMode.GROWTH,
            70_000, links = listOf(
                GoalAccountLink("a", GoalMode.GROWTH, 20_000, 1),
                GoalAccountLink("b", GoalMode.GROWTH, 40_000, 1)))
        // Account a fell below its snapshot and adds nothing; b still adds 10000.
        assertEquals(10_000L, goal.saved(listOf(a.copy(balance = 10_000), b.copy(balance = 50_000))))
    }

    @Test fun migrationSplitsBaselineAndPreservesProgress() {
        val accounts = listOf(a.copy(balance = 60_000), b.copy(balance = 40_000))
        val goal = SavingGoal("goal", "旅行", 500_000, setOf("a", "b"), GoalMode.GROWTH, 33_333, createdAt = 7)
        val legacy = SavingGoal("legacy", "旧", 500_000, setOf("a"))
        assertEquals(66_667L, goal.saved(accounts))
        val migrated = BannerAggregate.migrate(listOf(goal, legacy), accounts, now = 1_000L)
        val links = migrated.first().links
        assertEquals(2, links.size)
        assertEquals(33_333L, links.sumOf { it.baseline })
        assertTrue(links.all { it.baseline in 0..accounts.first { account -> account.id == it.accountId }.balance })
        assertTrue(links.all { it.baselineAt == 1_000L })
        // Progress is unchanged by the upgrade, and the current-balance goal is left alone.
        assertEquals(66_667L, migrated.first().saved(accounts))
        assertEquals(legacy, migrated.last())
        // Running the migration twice must not shift any snapshot.
        assertEquals(migrated, BannerAggregate.migrate(migrated, accounts, now = 2_000L))
    }

    @Test fun legacyGoalWithoutLinksStillAggregates() {
        val accounts = listOf(a.copy(balance = 60_000), b.copy(balance = 40_000))
        val goal = SavingGoal("goal", "旅行", 500_000, setOf("a", "b"), GoalMode.GROWTH, 25_000)
        // No links yet: the aggregate splits the goal-wide baseline on the fly.
        assertEquals(75_000L, BannerAggregate.saved(listOf(goal), accounts))
        assertEquals(goal.saved(accounts), BannerAggregate.saved(listOf(goal), accounts))
    }

    @Test fun migrationPreservesProgressWithMixedAndNegativeBalances() {
        val accounts = listOf(a, b.copy(balance = -50_000))
        for (baseline in listOf(-150_000L, 25_000L, 150_000L)) {
            val goal = SavingGoal("goal", "旅行", 500_000, setOf("a", "b"), GoalMode.GROWTH, baseline)
            val migrated = BannerAggregate.migrate(listOf(goal), accounts, now = 1_000L).single()
            assertEquals(baseline, migrated.links.sumOf { it.baseline })
            assertEquals(goal.saved(accounts), migrated.saved(accounts))
            assertEquals(goal.saved(accounts), BannerAggregate.saved(listOf(migrated), accounts))
        }
    }

    @Test fun migrationPreservesGrowthWhenAllBalancesAreNegative() {
        val accounts = listOf(a.copy(balance = -20_000), b.copy(balance = -10_000))
        val goal = SavingGoal("goal", "旅行", 500_000, setOf("a", "b"), GoalMode.GROWTH, -50_000)
        val migrated = BannerAggregate.migrate(listOf(goal), accounts, now = 1_000L).single()
        assertEquals(-50_000L, migrated.links.sumOf { it.baseline })
        assertEquals(20_000L, migrated.saved(accounts))
    }
}
