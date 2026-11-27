package com.kelsuita.starswish

import com.kelsuita.starswish.data.*
import org.junit.Assert.*
import org.junit.Test

class BackupDataTest {
    private fun backup() = BackupData(exportedAt = 1,
        accounts = listOf(BankAccount("account", "储蓄账户", 123456, 1)),
        goals = listOf(SavingGoal("goal", "旅行", 500000, setOf("account"), GoalMode.GROWTH, 100000, "2027-01-01")),
        appearance = Appearance(themeMode = 2, featuredGoalId = "goal", pureBlackBackground = true,
            predictiveBackEnabled = false),
        hitokoto = "慢慢来，比较快。",
        language = 3,
        quoteEnabled = false)

    @Test fun roundTripPreservesDataAndMoney() {
        val original = backup()
        assertEquals(original, BackupData.fromJson(original.toJson()))
        assertEquals(original, BackupData.fromJson("\uFEFF" + original.toJson()))
    }

    @Test fun emptyBackupIsValid() {
        val empty = BackupData(exportedAt = 0, accounts = emptyList(), goals = emptyList(), appearance = Appearance())
        assertEquals(empty, BackupData.fromJson(empty.toJson()))
    }

    @Test fun caseDistinctAccountNamesCanBeBackedUp() {
        val original = backup().copy(accounts = listOf(
            BankAccount("account", "Savings", 100, 1),
            BankAccount("other", "savings", 200, 1)))
        assertEquals(original, BackupData.fromJson(original.toJson()))
    }

    @Test fun legacyBackupWithoutCreationDatesStillImports() {
        val old = backup().toJson().replace(Regex(""",\s*"createdAt"\s*:\s*0"""), "")
        assertFalse(old.contains("createdAt"))
        val decoded = BackupData.fromJson(old)
        assertEquals(0L, decoded.accounts.single().createdAt)
        assertEquals(0L, decoded.goals.single().createdAt)
    }

    @Test fun creationDatesSurviveExportAndImport() {
        val original = backup().copy(
            accounts = backup().accounts.map { it.copy(createdAt = 123L) },
            goals = backup().goals.map { it.copy(createdAt = 456L) },
        )
        assertEquals(original, BackupData.fromJson(original.toJson()))
        assertThrows(IllegalArgumentException::class.java) {
            original.copy(goals = original.goals.map { it.copy(createdAt = -1L) }).toJson()
        }
    }

    @Test fun backupIncludesAllSettings() {
        val original = backup().copy(
            appearance = backup().appearance.copy(bannerAllGoals = true, collapseCardActions = true),
            developerMode = false, webDavBalanceEnabled = true, iosBarBlurRadius = 0,
            goals = backup().goals.map { goal ->
                goal.copy(links = listOf(GoalAccountLink("account", GoalMode.GROWTH, 100000L, 5L)))
            },
        )
        val json = original.toJson()
        assertEquals(original, BackupData.fromJson(json))
        assertTrue(json.contains("\"language\": 3"))
        assertTrue(json.contains("\"quoteEnabled\": false"))
        assertTrue(json.contains("\"predictiveBackEnabled\": false"))
        assertTrue(json.contains("\"collapseCardActions\": true"))
    }

    @Test fun backupWithoutTheCardMenuSwitchStillImports() {
        val json = backup().toJson().replace(Regex(""",\s*"collapseCardActions"\s*:\s*(?:true|false)"""), "")
        assertFalse(json.contains("collapseCardActions"))
        assertEquals(backup(), BackupData.fromJson(json))
        assertFalse(BackupData.fromJson(json).appearance.collapseCardActions)
    }

    /** The vault state rides along: a wish stays archived, with the day it was reached. */
    @Test fun vaultStateSurvivesExportAndImport() {
        val original = backup().copy(goals = backup().goals.map {
            it.copy(archived = true, archivedAt = 1764200000000L)
        })
        assertEquals(original, BackupData.fromJson(original.toJson()))

        // A backup written before the vault existed has neither field and still imports.
        val older = backup().toJson()
            .replace(Regex(""",\s*"archivedAt"\s*:\s*0"""), "")
            .replace(Regex(""",\s*"archived"\s*:\s*false"""), "")
        assertFalse(older.contains("archived"))
        val decoded = BackupData.fromJson(older).goals.single()
        assertFalse(decoded.archived)
        assertEquals(0L, decoded.archivedAt)
    }

    @Test fun oldBackupMayOmitNewSettings() {
        val json = backup().toJson().replace(
            Regex(""",\s*"(?:developerMode|webDavBalanceEnabled|iosBarBlurRadius)"\s*:\s*null"""), "",
        )
        assertFalse(json.contains("iosBarBlurRadius"))
        assertEquals(backup(), BackupData.fromJson(json))
    }

    @Test fun largeBackupRemainsImportable() {
        val accountList = List(50) { index -> BankAccount("$index-${"x".repeat(120)}", "Account $index", 100L, 1L) }
        val ids = accountList.map { it.id }.toSet()
        val links = accountList.map { GoalAccountLink(it.id, GoalMode.GROWTH, 50L, 1L) }
        val original = backup().copy(
            accounts = accountList,
            goals = List(90) { index ->
                SavingGoal("goal-$index", "Goal $index", 5000L, ids, GoalMode.GROWTH, 2500L, links = links)
            },
            appearance = Appearance(),
        )
        // Near the limit, expanded indentation must not make an exported file unreadable.
        val json = original.copy(goals = original.goals.take(60)).let { supported ->
            supported.toJson().also { assertEquals(supported, BackupData.fromJson(it)) }
        }
        assertFalse(json.contains('\n'))
        assertThrows(IllegalArgumentException::class.java) { original.toJson() }
    }

    @Test fun invalidBackupsAreRejected() {
        val original = backup()
        listOf(
            original.copy(version = 2),
            original.copy(format = "another-app"),
            original.copy(accounts = original.accounts + original.accounts),
            original.copy(accounts = emptyList()),
            original.copy(goals = listOf(original.goals.first().copy(target = 0))),
            original.copy(goals = listOf(original.goals.first().copy(deadline = "2027-13-30"))),
            original.copy(appearance = Appearance(accentIndex = 10)),
            original.copy(appearance = Appearance(bannerDecimals = 6)),
            original.copy(appearance = Appearance(featuredGoalId = "missing")),
            original.copy(language = AppLanguage.entries.size),
            original.copy(iosBarBlurRadius = -1),
            original.copy(iosBarBlurRadius = 41),
        ).forEach { value -> assertThrows(IllegalArgumentException::class.java) { value.toJson() } }
        assertThrows(IllegalArgumentException::class.java) { BackupData.fromJson("{}") }
        assertThrows(IllegalArgumentException::class.java) { BackupData.fromJson("x".repeat(BackupData.MAX_BYTES + 1)) }
    }

    @Test fun validationReasonSurvivesJsonParsing() {
        val badVersion = backup().toJson().replace("\"version\": 1", "\"version\": 2")
        val versionError = assertThrows(IllegalArgumentException::class.java) {
            BackupData.fromJson(badVersion)
        }
        assertTrue(versionError.message.orEmpty().contains("backup.error_unsupported"))
    }
}
