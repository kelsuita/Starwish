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
 * [missingAccountIds] may include dangling links left by older builds. Current deletions already
 * detach the links and retain only their names for the restore warning.
 */
fun restoreGoalFromVault(goal: SavingGoal, missingAccountIds: Set<String>): SavingGoal {
    return goal.withoutAccounts(missingAccountIds)
        .copy(archived = false, archivedAt = 0L, deletedLinkedAccounts = emptyMap())
}

/** Converts dangling references from older installs to restore-warning metadata. */
internal fun migrateMissingGoalAccounts(
    goal: SavingGoal,
    liveAccountIds: Set<String>,
    deletedNames: Map<String, String>,
    unknownName: String,
): SavingGoal {
    val missing = goal.accountIds - liveAccountIds
    if (missing.isEmpty()) return goal
    val detached = goal.withoutAccounts(missing)
    return if (goal.archived) detached.copy(deletedLinkedAccounts = goal.deletedLinkedAccounts +
        missing.associateWith { deletedNames[it] ?: goal.deletedLinkedAccounts[it] ?: unknownName }) else detached
}

private fun SavingGoal.withoutAccounts(ids: Set<String>): SavingGoal {
    if (ids.isEmpty()) return this
    val remainingIds = accountIds - ids
    val remainingLinks = links.filterNot { it.accountId in ids }
    return copy(
        accountIds = remainingIds,
        links = remainingLinks,
        baseline = when {
            remainingIds.isEmpty() -> 0L
            links.size == accountIds.size -> remainingLinks.sumOf { it.baseline }
            else -> baseline - links.filter { it.accountId in ids }.sumOf { it.baseline }
        },
    )
}

/**
 * Removes a deleted account from the wishes that link it, returning only the wishes that changed.
 *
 * Archiving does not exempt a wish: once the account is gone its balance no longer exists, so the
 * wish drops the id, the per-account snapshot and that snapshot's share of the baseline, and its
 * progress stops counting the deleted account.
 */
fun detachAccountFromGoals(goals: List<SavingGoal>, account: BankAccount): List<SavingGoal> =
    goals.mapNotNull { goal ->
        val accountId = account.id
        if (accountId !in goal.accountIds) return@mapNotNull null
        goal.withoutAccounts(setOf(accountId)).copy(
            deletedLinkedAccounts = if (goal.archived) goal.deletedLinkedAccounts + (accountId to account.name)
                else goal.deletedLinkedAccounts,
        )
    }
