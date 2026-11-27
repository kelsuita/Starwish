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
