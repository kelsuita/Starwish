package com.kelsuita.starswish.sync

import com.kelsuita.starswish.data.BankAccount
import com.kelsuita.starswish.data.Money
import com.kelsuita.starswish.data.SavingsStore
import com.kelsuita.starswish.data.tr
import java.util.UUID

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
) {
    fun updatedAccounts(
        accounts: List<BankAccount>,
        now: Long,
        newId: () -> String = { UUID.randomUUID().toString() },
    ): List<BankAccount> {
        val byId = changes.associateBy { it.accountId }
        require(byId.size == changes.size)
        val existing = accounts.associateBy { it.id }
        val nameCounts = accounts.groupingBy { it.name }.eachCount()
        changes.forEach { change ->
            val account = existing[change.accountId]
            require(account != null && account.name == change.name && account.balance == change.before &&
                nameCounts[change.name] == 1) {
                tr("common.save_failed")
            }
            require(change.after in -Money.MAX_CENTS..Money.MAX_CENTS)
        }
        require(additions.size <= WebDavAccountSelection.newAccountSlots(accounts.size)) {
            tr("account.error_max_accounts")
        }
        val names = accounts.map { it.name }.toMutableSet()
        additions.forEach { addition ->
            require(addition.name.isNotBlank() && addition.name.length <= 40)
            require(addition.cents in -Money.MAX_CENTS..Money.MAX_CENTS)
            require(names.add(addition.name)) { tr("account.error_name_exists") }
        }
        return accounts.map { account ->
            val change = byId[account.id]
            if (change == null) account else account.copy(balance = change.after, updatedAt = now,
                source = "account.source_webdav")
        } + additions.map { addition ->
            BankAccount(newId(), addition.name, addition.cents, now, "account.source_webdav", now)
        }
    }
}

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
        val remoteCounts = balances.filterNot { it.deleted }.groupingBy { it.name }.eachCount()
        // Locale-aware so Chinese account names sort the way the system lists them.
        val byName = java.text.Collator.getInstance()
        return balances.filter { !it.deleted }
            .sortedWith(Comparator { left, right -> byName.compare(left.name, right.name) })
            .map { account ->
                val creates = account.isCreatable(localCounts)
                val note = when {
                    account.currency != 0 -> "webdav.picker_currency"
                    account.name.isBlank() || account.name.length > MAX_NAME_LENGTH -> "webdav.picker_name"
                    (localCounts[account.name] ?: 0) > 1 || (remoteCounts[account.name] ?: 0) > 1 -> "webdav.picker_duplicate"
                    account.cents !in -Money.MAX_CENTS..Money.MAX_CENTS -> "webdav.picker_balance"
                    else -> null
                }
                WebDavPickerAccount(account.id, account.name, account.cents, note, creates && note == null)
            }
    }

    /** Rows ticked when the picker opens: existing local accounts only, never new ones. */
    fun defaultSelection(options: List<WebDavPickerAccount>): Set<Long> =
        options.filter { it.note == null && !it.creates }.map { it.id }.toSet()

    fun newAccountSlots(localCount: Int): Int = (SavingsStore.MAX_ACCOUNTS - localCount).coerceAtLeast(0)

    fun selectionNote(option: WebDavPickerAccount, selectedIds: Set<Long>, remainingSlots: Int): String? =
        option.note ?: if (option.creates && option.id !in selectedIds && remainingSlots <= 0)
            "webdav.picker_full" else null

    /** The import for the checked rows; unchecked and disabled rows change nothing. */
    fun plan(
        options: List<WebDavPickerAccount>,
        selectedIds: Set<Long>,
        localAccounts: List<BankAccount>,
    ): WebDavImportPlan {
        val selected = options.filter { it.note == null && it.id in selectedIds }
        val additions = selected.filter { it.creates }
        require(additions.size <= newAccountSlots(localAccounts.size)) { tr("account.error_max_accounts") }
        return WebDavImportPlan(
            changes = selected.filterNot { it.creates }.mapNotNull { option ->
                val account = localAccounts.singleOrNull { it.name == option.name }
                    ?: throw SyncError("webdav.error_match", option.name)
                if (account.balance == option.cents) null
                else WebDavBalanceChange(account.id, account.name, account.balance, option.cents)
            },
            additions = additions.map { WebDavAddition(it.name, it.cents) },
        )
    }

    private fun SnapshotAccountBalance.isCreatable(localCounts: Map<String, Int>) =
        !deleted && currency == 0 && name.isNotBlank() && name.length <= MAX_NAME_LENGTH && name !in localCounts
}
