package com.kelsuita.starswish.sync

import com.kelsuita.starswish.data.BankAccount
import org.junit.Assert.*
import org.junit.Test

class WebDavAccountSelectionTest {
    private fun local(name: String, balance: Long, id: String = name) = BankAccount(id, name, balance, 0)
    private fun backup(id: Long, name: String, cents: Long, currency: Int = 0, deleted: Boolean = false) =
        SnapshotAccountBalance(id, name, currency, cents, deleted)
    private fun option(options: List<WebDavPickerAccount>, name: String) = options.single { it.name == name }

    @Test fun optionsSortByName() {
        val options = WebDavAccountSelection.options(
            listOf(backup(1, "Charlie", 1), backup(2, "alpha", 2), backup(3, "Bravo", 3)),
            emptyList(),
        )
        assertEquals(listOf("alpha", "Bravo", "Charlie"), options.map { it.name })
    }

    @Test fun optionsDisableRowsThatCannotBeMatchedButOfferNewAccounts() {
        val options = WebDavAccountSelection.options(
            listOf(
                backup(1, "招商储蓄", 100),
                backup(2, "外币", 100, currency = 1),
                backup(3, "本地没有", 300),
                backup(4, "重名", 100),
                backup(5, "已删除", 100, deleted = true),
            ),
            listOf(local("招商储蓄", 50), local("重名", 1, "a"), local("重名", 2, "b")),
        )
        assertEquals(4, options.size)
        assertNull(option(options, "招商储蓄").note)
        assertFalse(option(options, "招商储蓄").creates)
        assertEquals("webdav.picker_currency", option(options, "外币").note)
        assertNull(option(options, "本地没有").note)
        assertTrue(option(options, "本地没有").creates)
        assertEquals("webdav.picker_duplicate", option(options, "重名").note)
    }

    @Test fun planCreatesMissingAccountsAndUpdatesSelectedBalances() {
        val options = listOf(
            WebDavPickerAccount(1, "招商储蓄", 100, null),
            WebDavPickerAccount(2, "微信", 500, null),
            WebDavPickerAccount(3, "新的卡", 700, null, creates = true),
            WebDavPickerAccount(4, "未选中", 300, null, creates = true),
        )
        val plan = WebDavAccountSelection.plan(options, setOf(1, 2, 3),
            listOf(local("招商储蓄", 50, "a"), local("微信", 500, "b")))
        assertEquals(listOf(WebDavBalanceChange("a", "招商储蓄", 50, 100)), plan.changes)
        assertEquals(listOf(WebDavAddition("新的卡", 700)), plan.additions)
    }

    @Test fun defaultSelectionTicksOnlyAccountsThatExistLocally() {
        val options = listOf(
            WebDavPickerAccount(1, "招商储蓄", 100, null),
            WebDavPickerAccount(2, "新的卡", 700, null, creates = true),
            WebDavPickerAccount(3, "外币", 100, "webdav.picker_currency"),
        )
        assertEquals(setOf(1L), WebDavAccountSelection.defaultSelection(options))
    }

    @Test fun disabledRowsAreIgnoredEvenWhenChecked() {
        val options = listOf(WebDavPickerAccount(1, "外币", 100, "webdav.picker_currency"))
        val plan = WebDavAccountSelection.plan(options, setOf(1), listOf(local("外币", 0)))
        assertTrue(plan.changes.isEmpty())
        assertTrue(plan.additions.isEmpty())
    }

    @Test fun optionsStopCreatingAccountsAtTheAccountLimit() {
        val full = WebDavAccountSelection.options(
            listOf(backup(1, "甲", 100), backup(2, "乙", 200)),
            (1..49).map { local("本地$it", 0, "id$it") },
        )
        assertEquals(listOf("webdav.picker_full", "webdav.picker_full"), full.map { it.note })

        // One free slot still offers a new account, but not two.
        val room = WebDavAccountSelection.options(
            listOf(backup(1, "甲", 100), backup(2, "乙", 200)),
            (1..48).map { local("本地$it", 0, "id$it") },
        )
        assertEquals(listOf(null, null), room.map { it.note })
    }
}
