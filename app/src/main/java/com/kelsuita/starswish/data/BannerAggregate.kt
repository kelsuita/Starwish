package com.kelsuita.starswish.data

import java.math.BigInteger

/**
 * Progress for the "add everything up" banner option.
 *
 * Every wish contributes an interval of an account's balance: current-balance wishes cover
 * `[0, balance]`, "start from now" wishes cover `[start, balance]`. Intervals of the same account
 * are merged, so an account shared by several wishes is only counted once, and a current-balance
 * wish swallows the account entirely.
 */
object BannerAggregate {

    data class Result(val saved: Long, val target: Long, val progress: Float)

    fun target(goals: List<SavingGoal>): Long = goals.sumOf { it.target }

    fun saved(goals: List<SavingGoal>, accounts: List<BankAccount>): Long {
        if (goals.isEmpty()) return 0L
        val balances = accounts.associate { it.id to it.balance }
        val perAccount = HashMap<String, MutableList<LongArray>>()
        goals.forEach { goal ->
            val starts = if (goal.mode == GoalMode.GROWTH) startsFor(goal, accounts) else emptyMap()
            goal.accountIds.forEach { accountId ->
                val balance = balances[accountId] ?: return@forEach
                val from = if (goal.mode == GoalMode.GROWTH) starts[accountId] ?: balance else 0L
                if (balance > from) {
                    perAccount.getOrPut(accountId) { ArrayList() }.add(longArrayOf(from, balance))
                }
            }
        }
        val merged = perAccount.values.sumOf { intervals -> mergedLength(intervals) }
        return merged.coerceIn(0L, target(goals))
    }

    fun result(goals: List<SavingGoal>, accounts: List<BankAccount>): Result {
        val total = target(goals)
        val saved = saved(goals, accounts)
        return Result(saved, total, if (total <= 0L) 0f else
            (saved.toDouble() / total).toFloat().coerceIn(0f, 1f))
    }

    /** Start snapshot of [accountId] for a "start from now" wish. */
    fun startFor(goal: SavingGoal, accountId: String, accounts: List<BankAccount>): Long? {
        val balances = accounts.associate { it.id to it.balance }
        val balance = balances[accountId] ?: return null
        if (goal.mode != GoalMode.GROWTH) return 0L
        return startsFor(goal, accounts)[accountId] ?: balance
    }

    /**
     * Splits a legacy goal-wide baseline across its linked accounts in proportion to their current
     * balances, so the wish keeps exactly the progress it showed before the upgrade while every
     * account gets its own snapshot. Largest-remainder rounding keeps the sum exact.
     */
    fun splitBaseline(baseline: Long, accounts: List<BankAccount>, at: Long): List<GoalAccountLink> {
        if (accounts.isEmpty()) return emptyList()
        val limit = accounts.size * Money.MAX_CENTS
        val usable = baseline.coerceIn(-limit, limit)
        val current = accounts.sumOf { it.balance }
        if (usable == current) return accounts.map { GoalAccountLink(it.id, GoalMode.GROWTH, it.balance, at) }

        // The usual case splits the baseline itself. With negative balances or a baseline outside
        // the current total, split the net difference in one direction so clamping each account's
        // growth at zero still preserves the old goal's displayed progress.
        val proportional = accounts.all { it.balance >= 0L } && usable in 0L..current
        val increase = usable > current
        val weights = accounts.map {
            when {
                proportional -> it.balance
                increase -> Money.MAX_CENTS - it.balance
                else -> it.balance + Money.MAX_CENTS
            }
        }
        val total = weights.sum()
        val amount = if (proportional) usable else if (increase) usable - current else current - usable
        val bigTotal = BigInteger.valueOf(total)
        val bigAmount = BigInteger.valueOf(amount)
        val shares = weights.map { weight ->
            bigAmount.multiply(BigInteger.valueOf(weight)).divide(bigTotal)
        }.toMutableList()
        var deficit = bigAmount.subtract(shares.fold(BigInteger.ZERO, BigInteger::add))
        val order = accounts.indices.sortedWith(
            compareByDescending<Int> { bigAmount.multiply(BigInteger.valueOf(weights[it])).mod(bigTotal) }
                .thenBy { it })
        var cursor = 0
        while (deficit.signum() > 0) {
            val index = order[cursor % order.size]
            shares[index] = shares[index].add(BigInteger.ONE)
            deficit = deficit.subtract(BigInteger.ONE)
            cursor++
        }
        return accounts.mapIndexed { index, account ->
            val share = shares[index].toLong()
            val start = when {
                proportional -> share
                increase -> account.balance + share
                else -> account.balance - share
            }
            GoalAccountLink(account.id, GoalMode.GROWTH, start, at)
        }
    }

    /**
     * Fills missing account-level snapshots. Idempotent: goals that already carry a complete set
     * of links are returned untouched, which also makes an interrupted migration safe to retry.
     */
    fun migrate(goals: List<SavingGoal>, accounts: List<BankAccount>, now: Long): List<SavingGoal> =
        goals.map { goal ->
            if (goal.mode != GoalMode.GROWTH || goal.links.size == goal.accountIds.size) goal
            else {
                val linked = accounts.filter { it.id in goal.accountIds }
                val links = splitBaseline(goal.baseline, linked, now)
                goal.copy(links = links, baseline = links.sumOf { it.baseline })
            }
        }

    private fun startsFor(goal: SavingGoal, accounts: List<BankAccount>): Map<String, Long> {
        if (goal.links.size == goal.accountIds.size) return goal.links.associate { it.accountId to it.baseline }
        // Un-migrated data still renders correctly by splitting the goal-wide baseline on the fly.
        val linked = accounts.filter { it.id in goal.accountIds }
        return splitBaseline(goal.baseline, linked, goal.createdAt).associate { it.accountId to it.baseline }
    }

    private fun mergedLength(intervals: List<LongArray>): Long {
        if (intervals.isEmpty()) return 0L
        val sorted = intervals.sortedBy { it[0] }
        var total = 0L
        var start = sorted.first()[0]
        var end = sorted.first()[1]
        sorted.drop(1).forEach { interval ->
            if (interval[0] > end) {
                total += end - start
                start = interval[0]
                end = interval[1]
            } else if (interval[1] > end) {
                end = interval[1]
            }
        }
        return total + (end - start)
    }
}
