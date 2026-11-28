package com.kelsuita.starswish.data

/**
 * Applies the account editor's checkbox draft to the wishes that link this account.
 *
 * Linking adds the account to the wish; a "start from now" wish also takes a snapshot of the
 * balance the account joins with, so its already-accumulated part is not credited retroactively.
 * Unlinking removes the account and the snapshot it brought; a wish may end up with no account at
 * all, which the card shows as 「没有关联账户」.
 */
fun linkAccountToGoals(
    account: BankAccount,
    goals: List<SavingGoal>,
    linkedGoalIds: Set<String>,
    now: Long = System.currentTimeMillis(),
): List<SavingGoal> = goals.mapNotNull { goal ->
    val wasLinked = account.id in goal.accountIds
    val willLink = goal.id in linkedGoalIds
    when {
        wasLinked == willLink -> null
        willLink -> {
            val start = if (goal.mode == GoalMode.GROWTH) account.balance else 0L
            goal.copy(
                accountIds = goal.accountIds + account.id,
                links = if (goal.mode == GoalMode.GROWTH) {
                    goal.links + GoalAccountLink(account.id, GoalMode.GROWTH, start, now)
                } else {
                    goal.links
                },
                baseline = goal.baseline + start,
            )
        }
        else -> {
            val start = goal.links.firstOrNull { it.accountId == account.id }?.baseline ?: 0L
            goal.copy(
                accountIds = goal.accountIds - account.id,
                links = goal.links.filterNot { it.accountId == account.id },
                baseline = (goal.baseline - start).coerceAtLeast(0L),
            )
        }
    }
}

/**
 * Takes a wish back out of the vault.
 *
 * [missingAccountIds] are accounts that were deleted while the wish was archived: a vaulted wish
 * does not block a deletion, so its frozen account list may point at accounts that no longer
 * exist. Dropping them here keeps the wish from carrying dead ids and snapshots, and its progress
 * stops counting what those accounts had contributed.
 */
fun restoreGoalFromVault(goal: SavingGoal, missingAccountIds: Set<String>): SavingGoal {
    if (missingAccountIds.isEmpty()) return goal.copy(archived = false, archivedAt = 0L)
    val removedBaseline = goal.links.filter { it.accountId in missingAccountIds }.sumOf { it.baseline }
    return goal.copy(
        archived = false,
        archivedAt = 0L,
        accountIds = goal.accountIds - missingAccountIds,
        links = goal.links.filterNot { it.accountId in missingAccountIds },
        baseline = (goal.baseline - removedBaseline).coerceAtLeast(0L),
    )
}

/**
 * Removes a deleted account from the wishes that link it, returning only the wishes that changed.
 *
 * Archiving does not exempt a wish: once the account is gone its balance no longer exists, so the
 * wish drops the id, the per-account snapshot and that snapshot's share of the baseline, and its
 * progress stops counting the deleted account.
 */
fun detachAccountFromGoals(goals: List<SavingGoal>, accountId: String): List<SavingGoal> =
    goals.mapNotNull { goal ->
        if (accountId !in goal.accountIds) return@mapNotNull null
        val removedBaseline = goal.links.filter { it.accountId == accountId }.sumOf { it.baseline }
        goal.copy(
            accountIds = goal.accountIds - accountId,
            links = goal.links.filterNot { it.accountId == accountId },
            baseline = (goal.baseline - removedBaseline).coerceAtLeast(0L),
        )
    }
