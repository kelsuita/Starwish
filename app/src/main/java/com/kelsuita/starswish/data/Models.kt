package com.kelsuita.starswish.data

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import kotlinx.serialization.Serializable

object Money {
    const val MAX_CENTS = 99_999_999_999_999L
    fun parse(text: String): Long? {
        val clean = text.trim().replace(",", "").replace("，", "")
            .replace("￥", "").replace("¥", "").removeSuffix("元").trim()
        if (!Regex("[+-]?\\d{1,12}(\\.\\d{1,2})?").matches(clean)) return null
        return runCatching {
            BigDecimal(clean).movePointRight(2).setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact().takeIf { it in -MAX_CENTS..MAX_CENTS }
        }.getOrNull()
    }
    fun input(cents: Long): String = BigDecimal.valueOf(cents, 2).toPlainString()
    fun format(cents: Long): String = NumberFormat.getNumberInstance(Locale.CHINA).apply {
        minimumFractionDigits = 2; maximumFractionDigits = 2
    }.format(BigDecimal.valueOf(cents, 2))
}

@Serializable
data class BankAccount(
    val id: String,
    val name: String,
    val balance: Long,
    val updatedAt: Long,
    val source: String = "account.source_manual",
    val createdAt: Long = 0L,
)

@Serializable
enum class GoalMode { BALANCE, GROWTH }

/**
 * Per wish-account start snapshot. A goal keeps one link per linked account so several wishes
 * that share an account can still be de-duplicated when their progress is merged.
 */
@Serializable
data class GoalAccountLink(
    val accountId: String,
    val mode: GoalMode,
    /** Balance of [accountId] when "start from now" was switched on (0 for current-balance goals). */
    val baseline: Long,
    val baselineAt: Long,
)

@Serializable
data class SavingGoal(
    val id: String,
    val title: String,
    val target: Long,
    val accountIds: Set<String>,
    val mode: GoalMode = GoalMode.BALANCE,
    val baseline: Long = 0L,
    val deadline: String = "",
    val colorIndex: Int = 0,
    val createdAt: Long = 0L,
    /** Account-level snapshots; empty for goals written before this field existed. */
    val links: List<GoalAccountLink> = emptyList(),
    /** Set once the wish moved into 珍藏馆; it leaves the active list and stops counting toward
     *  the banner, and its card can only be taken back out of the vault. */
    val archived: Boolean = false,
    /** When the wish entered 珍藏馆; 0 for wishes that never left the active list. */
    val archivedAt: Long = 0L,
    /** Display names of accounts detached by deletion while this wish was in the vault. */
    val deletedLinkedAccounts: Map<String, String> = emptyMap(),
) {
    fun saved(accounts: List<BankAccount>): Long {
        val linked = accounts.filter { it.id in accountIds }
        // With per-account snapshots each account contributes only what it gained, so one
        // account that fell below its own start snapshot no longer drags down the others.
        if (mode == GoalMode.GROWTH && links.size == accountIds.size) {
            val starts = links.associate { it.accountId to it.baseline }
            return linked.sumOf { account ->
                val start = starts[account.id] ?: account.balance
                (account.balance - start).coerceAtLeast(0L)
            }.coerceAtLeast(0L)
        }
        return (linked.sumOf { it.balance } - if (mode == GoalMode.GROWTH) baseline else 0L)
            .coerceAtLeast(0L)
    }
    fun progress(accounts: List<BankAccount>): Float =
        if (target <= 0) 0f else (saved(accounts).toDouble() / target).toFloat().coerceIn(0f, 1f)
    fun remaining(accounts: List<BankAccount>): Long = (target - saved(accounts)).coerceAtLeast(0L)
}

data class BalanceCandidate(val accountId: String, val balance: Long, val capturedAt: Long)
data class CaptureRequest(val accountId: String, val expiresAt: Long)
