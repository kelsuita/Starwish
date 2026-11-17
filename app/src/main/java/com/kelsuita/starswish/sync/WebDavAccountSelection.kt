package com.kelsuita.starswish.sync

import com.kelsuita.starswish.data.BankAccount
import com.kelsuita.starswish.data.SavingsStore

/**
 * One account found in the Xiaoxing backup and offered by the WebDAV picker.
 * [note] is the translation key explaining why an account cannot be imported; null means it is
 * selectable. [creates] marks accounts that have no local same-name counterpart and would be
 * added as new accounts.
 */
internal data class WebDavPickerAccount(
    val id: Long,
    val name: String,
    val cents: Long,
    val note: String?,
    val creates: Boolean = false,
)

/** One local account whose balance will be replaced. */
internal data class WebDavBalanceChange(val accountId: String, val name: String, val before: Long, val after: Long)

/** One local account created from a backup account that has no same-name counterpart. */
internal data class WebDavAddition(val name: String, val cents: Long)

/** Everything the confirmation sheet shows and the import applies. */
internal data class WebDavImportPlan(
    val changes: List<WebDavBalanceChange>,
    val additions: List<WebDavAddition>,
)

/**
 * Turns the accounts parsed from a backup into the rows of the WebDAV picker and applies the
 * user's selection. Kept free of Compose state so the matching rules can be unit tested.
 */
internal object WebDavAccountSelection {
    /** Local accounts are matched by name, so names must be unique and fit the account editor. */
    private const val MAX_NAME_LENGTH = 40

    /**
     * Accounts that cannot be imported are offered as disabled rows instead of failing the whole
     * read; accounts without a local counterpart are selectable and become new local accounts, but
     * stay unticked by default so a read only touches accounts that already exist locally.
     */
    fun options(balances: List<SnapshotAccountBalance>, localAccounts: List<BankAccount>): List<WebDavPickerAccount> {
        val localCounts = localAccounts.groupingBy { it.name }.eachCount()
        val roomForNew = (SavingsStore.MAX_ACCOUNTS - localAccounts.size).coerceAtLeast(0)
        val newCount = balances.count { it.isCreatable(localCounts) }
        // Locale-aware so Chinese account names sort the way the system lists them.
        val byName = java.text.Collator.getInstance()
        return balances.filter { !it.deleted }
            .sortedWith(Comparator { left, right -> byName.compare(left.name, right.name) })
            .map { account ->
                val creates = account.isCreatable(localCounts)
                val note = when {
                    account.currency != 0 -> "webdav.picker_currency"
                    account.name.isBlank() || account.name.length > MAX_NAME_LENGTH -> "webdav.picker_name"
                    (localCounts[account.name] ?: 0) > 1 -> "webdav.picker_duplicate"
                    creates && newCount > roomForNew -> "webdav.picker_full"
                    else -> null
                }
                WebDavPickerAccount(account.id, account.name, account.cents, note, creates && note == null)
            }
    }

    /** Rows ticked when the picker opens: existing local accounts only, never new ones. */
    fun defaultSelection(options: List<WebDavPickerAccount>): Set<Long> =
        options.filter { it.note == null && !it.creates }.map { it.id }.toSet()

    /** The import for the checked rows; unchecked and disabled rows change nothing. */
    fun plan(
        options: List<WebDavPickerAccount>,
        selectedIds: Set<Long>,
        localAccounts: List<BankAccount>,
    ): WebDavImportPlan {
        val selected = options.filter { it.note == null && it.id in selectedIds }
        return WebDavImportPlan(
            changes = selected.mapNotNull { option ->
                val account = localAccounts.singleOrNull { it.name == option.name } ?: return@mapNotNull null
                if (account.balance == option.cents) null
                else WebDavBalanceChange(account.id, account.name, account.balance, option.cents)
            },
            additions = selected.filter { it.creates }.map { WebDavAddition(it.name, it.cents) },
        )
    }

    private fun SnapshotAccountBalance.isCreatable(localCounts: Map<String, Int>) =
        !deleted && currency == 0 && name.isNotBlank() && name.length <= MAX_NAME_LENGTH && name !in localCounts
}
