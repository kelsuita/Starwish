package com.kelsuita.starswish.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class SortSettings(
    val criterion: Int = -1,
    val descending: Boolean = false,
    val manualOrder: List<String>? = null,
)

/** One private preference file. Writes update memory immediately and flush to disk asynchronously. */
class SavingsStore(context: Context) {
    companion object {
        /** Hard cap enforced by [saveAccount]; the WebDAV picker checks it before creating accounts. */
        const val MAX_ACCOUNTS = 50
    }
    private val prefs = context.applicationContext.getSharedPreferences("savings_v1", Context.MODE_PRIVATE)

    fun developerModeEnabled(): Boolean = prefs.getBoolean("developer_mode", false)
    fun setDeveloperMode(enabled: Boolean) {
        prefs.edit().putBoolean("developer_mode", enabled).apply()
    }
    fun iosBarBlurRadius(): Int = prefs.getInt("ios_bar_blur_radius", IOS_BAR_BLUR_DEFAULT_PX)
        .coerceIn(0, IOS_BAR_BLUR_MAX_PX)
    fun setIosBarBlurRadius(radius: Int) {
        prefs.edit().putInt("ios_bar_blur_radius", radius.coerceIn(0, IOS_BAR_BLUR_MAX_PX)).apply()
    }
    fun webDavBalanceEnabled(): Boolean = prefs.getBoolean("webdav_balance_enabled", false)
    fun setWebDavBalanceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("webdav_balance_enabled", enabled).apply()
    }
    fun webDavUrl(): String = prefs.getString("webdav_url", "").orEmpty()
    fun webDavAccount(): String = prefs.getString("webdav_account", "").orEmpty()
    fun encryptedWebDavPassword(): String = prefs.getString("webdav_password", "").orEmpty()
    /** Connected means saved credentials plus at least one confirmed balance import. */
    fun webDavConnected(): Boolean = prefs.getBoolean("webdav_connected", false) &&
        webDavAccount().isNotBlank() && encryptedWebDavPassword().isNotBlank()
    fun markWebDavConnected() {
        prefs.edit().putBoolean("webdav_connected", true).apply()
    }
    fun markWebDavDisconnected() {
        prefs.edit().putBoolean("webdav_connected", false).apply()
    }
    /**
     * Saves credentials. [credentialsChanged] is decided by the caller because the stored
     * password is encrypted with a random IV, so comparing ciphertext would always report a
     * change even when the plaintext is identical.
     */
    fun saveWebDavSettings(url: String, account: String, encryptedPassword: String, credentialsChanged: Boolean) {
        val editor = prefs.edit().putString("webdav_url", url.trim())
            .putString("webdav_account", account.trim())
            .putString("webdav_password", encryptedPassword)
        if (credentialsChanged) editor.putBoolean("webdav_connected", false)
        editor.apply()
    }

    fun hitokoto(): String? = prefs.getString("hitokoto", null)
    fun saveHitokoto(value: String) = put("hitokoto", value)

    /**
     * Quote (一言) choice. Null means the user has not chosen yet, so the effective default
     * follows the UI language (on for Chinese). A stored value is the user's explicit choice and
     * is preserved when the language changes; non-Chinese UI hides the quote regardless.
     */
    fun quoteEnabled(): Boolean? =
        if (prefs.contains("quote_enabled")) prefs.getBoolean("quote_enabled", true) else null
    fun saveQuoteEnabled(value: Boolean) {
        prefs.edit().putBoolean("quote_enabled", value).apply()
    }

    /** UI language index: an index into [AppLanguage.entries]. */
    fun language(): Int {
        if (prefs.contains("language_v2")) return prefs.getInt("language_v2", 0).coerceIn(0, AppLanguage.entries.lastIndex)
        // Builds before the follow-system option stored 0..2 directly; shift those by one so an
        // explicit choice survives the update instead of silently becoming "follow system".
        val legacy = if (prefs.contains("language")) prefs.getInt("language", 1) else -1
        return if (legacy in 0..2) legacy + 1 else 0
    }
    fun saveLanguage(value: Int) {
        require(value in AppLanguage.entries.indices)
        prefs.edit().putInt("language_v2", value).apply()
    }

    fun appearance(): Appearance = Appearance(
        prefs.getInt("theme_mode", 0).coerceIn(0, 2),
        prefs.getInt("accent_choice", if (prefs.contains("accent_index"))
            prefs.getInt("accent_index", 0) + 2 else 0).coerceIn(0, 5),
        prefs.getInt("banner_style", 0).coerceIn(0, 1),
        prefs.getString("featured_goal", "").orEmpty(),
        prefs.getInt("palette_style", 0).coerceIn(0, 8),
        prefs.getInt("color_spec", 1).coerceIn(0, 1),
        prefs.getBoolean("blur_enabled", false),
        prefs.getBoolean("pure_black_background", false),
        prefs.getBoolean("floating_nav_bar", false),
        prefs.getInt("floating_nav_bar_style", 0).coerceIn(0, 1),
        prefs.getBoolean("predictive_back_enabled", true),
        prefs.getInt("banner_decimals", 0).coerceIn(0, 5),
        prefs.getBoolean("banner_all_goals", false),
        prefs.getBoolean("collapse_card_actions", false)
    )
    fun saveAppearance(value: Appearance) {
        require(value.themeMode in 0..2 && value.accentIndex in 0..5 && value.bannerStyle in 0..1 &&
            value.paletteStyle in 0..8 && value.colorSpec in 0..1 && value.floatingNavBarStyle in 0..1 &&
            value.bannerDecimals in 0..5)
        prefs.edit().putInt("theme_mode", value.themeMode)
            .putInt("accent_choice", value.accentIndex)
            .putInt("banner_style", value.bannerStyle)
            .putString("featured_goal", value.featuredGoalId)
            .putInt("palette_style", value.paletteStyle)
            .putInt("color_spec", value.colorSpec)
            .putBoolean("blur_enabled", value.blurEnabled)
            .putBoolean("pure_black_background", value.pureBlackBackground)
            .putBoolean("floating_nav_bar", value.floatingNavBar)
            .putInt("floating_nav_bar_style", value.floatingNavBarStyle)
            .putBoolean("collapse_card_actions", value.collapseCardActions)
            .putBoolean("predictive_back_enabled", value.predictiveBackEnabled)
            .putInt("banner_decimals", value.bannerDecimals)
            .putBoolean("banner_all_goals", value.bannerAllGoals).apply()
    }

    @Synchronized fun exportBackup(): BackupData {
        val goals = goals()
        val appearance = appearance().let { value ->
            if (goals.any { it.id == value.featuredGoalId }) value else value.copy(featuredGoalId = "")
        }
        return BackupData(exportedAt = System.currentTimeMillis(), accounts = accounts(), goals = goals,
            appearance = appearance, hitokoto = hitokoto(), language = language(),
            quoteEnabled = quoteEnabled(), developerMode = developerModeEnabled(),
            webDavBalanceEnabled = webDavBalanceEnabled(), iosBarBlurRadius = iosBarBlurRadius()).validate()
    }

    @Synchronized fun importBackup(backup: BackupData) {
        backup.validate()
        val value = backup.appearance
        val restoredAccounts = backup.accounts.mapIndexed { i, account ->
            if (account.createdAt == 0L) account.copy(createdAt = i.toLong() + 1) else account
        }
        val restoredGoals = BannerAggregate.migrate(backup.goals.mapIndexed { i, goal ->
            if (goal.createdAt == 0L) goal.copy(createdAt = i.toLong() + 1) else goal
        }, restoredAccounts, System.currentTimeMillis())
        // Commit the complete replacement once, only after every field has passed validation.
        val editor = prefs.edit()
            .putString("accounts", accountsJson(restoredAccounts).toString())
            .putString("goals", JSONArray().apply { restoredGoals.forEach { put(goalJson(it)) } }.toString())
            .putInt("theme_mode", value.themeMode).putInt("accent_choice", value.accentIndex)
            .putInt("banner_style", value.bannerStyle).putString("featured_goal", value.featuredGoalId)
.putInt("palette_style", value.paletteStyle)
            .putInt("color_spec", value.colorSpec).putBoolean("blur_enabled", value.blurEnabled)
            .putBoolean("pure_black_background", value.pureBlackBackground)
            .putBoolean("floating_nav_bar", value.floatingNavBar)
            .putInt("floating_nav_bar_style", value.floatingNavBarStyle)
            .putBoolean("predictive_back_enabled", value.predictiveBackEnabled)
            .putInt("banner_decimals", value.bannerDecimals)
            .putBoolean("banner_all_goals", value.bannerAllGoals)
            .putBoolean("collapse_card_actions", value.collapseCardActions)
            .putBoolean("goal_links_v2", true)
            .putInt("language_v2", backup.language)
            .remove("goal_sort").remove("account_sort")
            .remove("capture").remove("candidate")
        // WebDAV credentials and connection state stay on this device.
        backup.developerMode?.let { editor.putBoolean("developer_mode", it) }
        backup.webDavBalanceEnabled?.let { editor.putBoolean("webdav_balance_enabled", it) }
        backup.iosBarBlurRadius?.let { editor.putInt("ios_bar_blur_radius", it) }
        if (backup.quoteEnabled == null) editor.remove("quote_enabled")
        else editor.putBoolean("quote_enabled", backup.quoteEnabled)
        if (backup.hitokoto == null) editor.remove("hitokoto") else editor.putString("hitokoto", backup.hitokoto)
        check(editor.commit()) { tr("backup.error_import_save") }
    }

    @Synchronized fun accounts(): List<BankAccount> = readArray("accounts").let { array ->
        (0 until array.length()).mapNotNull { i ->
            runCatching {
                array.getJSONObject(i).let {
                    BankAccount(it.getString("id"), it.getString("name"), it.getLong("balance"),
                        it.getLong("updatedAt"), it.optString("source", "account.source_manual"), it.optLong("createdAt", i.toLong() + 1))
                }
            }.getOrNull()
        }
    }
    @Synchronized fun goals(): List<SavingGoal> = readArray("goals").let { array ->
        (0 until array.length()).mapNotNull { i ->
            runCatching {
                array.getJSONObject(i).let { o ->
                    val ids = o.optJSONArray("accountIds") ?: JSONArray()
                    val accountIds = (0 until ids.length()).map { ids.getString(it) }.toSet()
                    SavingGoal(o.getString("id"), o.getString("title"), o.getLong("target"), accountIds,
                        runCatching { GoalMode.valueOf(o.optString("mode", "BALANCE")) }
                            .getOrDefault(GoalMode.BALANCE), o.optLong("baseline"),
                        o.optString("deadline"), o.optInt("colorIndex"), o.optLong("createdAt", i.toLong() + 1),
                        linksFrom(o, accountIds), o.optBoolean("archived", false), o.optLong("archivedAt", 0L))
                }
            }.getOrNull()
        }
    }
    @Synchronized fun saveAccount(account: BankAccount) {
        require(account.name.isNotBlank() && account.name.length <= 40)
        require(account.balance in -Money.MAX_CENTS..Money.MAX_CENTS)
        val existing = accounts()
        require(existing.none { it.id != account.id && it.name == account.name }) { tr("account.error_name_exists") }
        require(existing.any { it.id == account.id } || existing.size < MAX_ACCOUNTS) { tr("account.error_max_accounts") }
        writeAccounts(existing.map { if (it.id == account.id) account.copy(createdAt = it.createdAt) else it }
            .let { if (existing.any { it.id == account.id }) it else it + account.copy(createdAt = System.currentTimeMillis()) })
    }
    @Synchronized fun saveAccountBalances(balances: Map<String, Long>, updatedAt: Long) {
        val existing = accounts()
        require(balances.isNotEmpty() && balances.keys.all { id -> existing.any { it.id == id } })
        require(balances.values.all { it in -Money.MAX_CENTS..Money.MAX_CENTS })
        val updated = existing.map { account ->
            balances[account.id]?.let { cents ->
                account.copy(balance = cents, updatedAt = updatedAt, source = "account.source_webdav")
            } ?: account
        }
        writeAccounts(updated)
    }
    @Synchronized fun saveGoal(goal: SavingGoal) {
        require(goal.title.isNotBlank() && goal.title.length <= 40)
        require(goal.target in 1..Money.MAX_CENTS)
        require(goal.accountIds.all { id -> accounts().any { it.id == id } })
        val existing = goals()
        require(existing.any { it.id == goal.id } || existing.size < 100) { tr("goal.error_max_goals") }
        // Account-level snapshots are only meaningful for "start from now"; drop anything stale so
        // a wish can never carry a link for an account it no longer uses.
        val normalized = goal.copy(links = if (goal.mode == GoalMode.GROWTH)
            goal.links.filter { it.accountId in goal.accountIds }
        else emptyList())
        put("goals", JSONArray().apply {
            existing.map { if (it.id == goal.id) normalized.copy(createdAt = it.createdAt) else it }
                .let { if (existing.any { it.id == goal.id }) it else it + normalized.copy(createdAt = System.currentTimeMillis()) }
                .forEach { put(goalJson(it)) }
        }.toString())
    }
    /**
     * One-time upgrade from a goal-wide baseline to per-account snapshots.
     */
    @Synchronized fun migrateGoalLinks(now: Long = System.currentTimeMillis()) {
        if (prefs.getBoolean("goal_links_v2", false)) return
        val current = goals()
        val migrated = BannerAggregate.migrate(current, accounts(), now)
        put("goals", JSONArray().apply { migrated.forEach { put(goalJson(it)) } }.toString())
        prefs.edit().putBoolean("goal_links_v2", true).apply()
    }
    fun sortSettings(goals: Boolean): SortSettings = runCatching {
        val json = JSONObject(prefs.getString(sortKey(goals), "{}") ?: "{}")
        val order = json.optJSONArray("manualOrder")
        SortSettings(json.optInt("criterion", -1), json.optBoolean("descending"),
            order?.let { array -> List(array.length()) { array.getString(it) } })
    }.getOrDefault(SortSettings())

    @Synchronized fun reorderGoals(ids: List<String>, settings: SortSettings) {
        val current = goals()
        require(ids.size == current.size && ids.toSet().size == ids.size && ids.toSet() == current.map { it.id }.toSet())
        val byId = current.associateBy { it.id }
        prefs.edit().putString("goals", JSONArray().apply {
            ids.forEach { put(goalJson(byId.getValue(it))) }
        }.toString()).writeSortSettings(true, settings).apply()
    }
    @Synchronized fun reorderAccounts(ids: List<String>, settings: SortSettings) {
        val current = accounts()
        require(ids.size == current.size && ids.toSet().size == ids.size && ids.toSet() == current.map { it.id }.toSet())
        val byId = current.associateBy { it.id }
        prefs.edit().putString("accounts", accountsJson(ids.map { byId.getValue(it) }).toString())
            .writeSortSettings(false, settings).apply()
    }

    private fun sortKey(goals: Boolean) = if (goals) "goal_sort" else "account_sort"

    private fun SharedPreferences.Editor.writeSortSettings(goals: Boolean, settings: SortSettings) =
        putString(sortKey(goals), JSONObject()
            .put("criterion", settings.criterion)
            .put("descending", settings.descending)
            .put("manualOrder", settings.manualOrder?.let { JSONArray(it) }).toString())
    @Synchronized fun deleteGoal(id: String) {
        val remaining = goals().filterNot { it.id == id }
        val editor = prefs.edit()
            .putString("goals", JSONArray().apply { remaining.forEach { put(goalJson(it)) } }.toString())
        // A dangling featured id would leave the home banner falling back to the first goal,
        // while settings still highlighted a missing entry.
        if (prefs.getString("featured_goal", "") == id) editor.putString("featured_goal", "")
        editor.apply()
    }
    @Synchronized fun deleteAccount(id: String) {
        // Wishes in the vault do not block a deletion: they are frozen and cannot be edited, so
        // their account list is left as it is and the account's name is kept aside for the
        // explanation shown when such a wish comes back to the active list.
        require(goals().none { !it.archived && id in it.accountIds }) { tr("account.error_unlink_first") }
        val removed = accounts().firstOrNull { it.id == id }
        writeAccounts(accounts().filterNot { it.id == id })
        if (removed != null && goals().any { it.archived && id in it.accountIds }) {
            val names = deletedAccountNamesJson()
            names.put(id, removed.name)
            prefs.edit().putString("deleted_account_names", names.toString()).apply()
        }
        if (candidate()?.accountId == id) clearCandidate()
        if (request()?.accountId == id) cancelCapture()
    }
    /** Account id to name, kept for accounts deleted while a vaulted wish still linked them. */
    private fun deletedAccountNamesJson(): JSONObject =
        runCatching { JSONObject(prefs.getString("deleted_account_names", "{}") ?: "{}") }
            .getOrElse { JSONObject() }
    fun deletedAccountNames(ids: Collection<String>): Map<String, String> {
        val names = deletedAccountNamesJson()
        return ids.mapNotNull { id ->
            names.optString(id, "").takeIf { it.isNotEmpty() }?.let { id to it }
        }.toMap()
    }
    @Synchronized fun forgetDeletedAccountNames(ids: Collection<String>) {
        if (ids.isEmpty()) return
        val names = deletedAccountNamesJson()
        ids.forEach { names.remove(it) }
        prefs.edit().putString("deleted_account_names", names.toString()).apply()
    }
    fun request(): CaptureRequest? = runCatching {
        prefs.getString("capture", null)?.let {
            JSONObject(it).let { o -> CaptureRequest(o.getString("accountId"), o.getLong("expiresAt")) }
        }
    }.getOrNull()
    fun startCapture(id: String) {
        require(accounts().any { it.id == id })
        prefs.edit().remove("candidate").putString("capture", JSONObject()
            .put("accountId", id).put("expiresAt", System.currentTimeMillis() + 120_000).toString()).apply()
    }
    fun cancelCapture() { prefs.edit().remove("capture").apply() }
    fun candidate(): BalanceCandidate? = runCatching {
        prefs.getString("candidate", null)?.let {
            JSONObject(it).let { o -> BalanceCandidate(o.getString("accountId"), o.getLong("balance"), o.getLong("capturedAt")) }
        }
    }.getOrNull()
    fun stage(candidate: BalanceCandidate) {
        prefs.edit().putString("candidate", JSONObject().put("accountId", candidate.accountId)
            .put("balance", candidate.balance).put("capturedAt", candidate.capturedAt).toString())
            .remove("capture").apply()
    }
    fun clearCandidate() { prefs.edit().remove("candidate").apply() }
    @Synchronized fun acceptCandidate() {
        val candidate = candidate() ?: return
        require(candidate.balance in -Money.MAX_CENTS..Money.MAX_CENTS)
        val account = accounts().firstOrNull { it.id == candidate.accountId } ?: error(tr("account.error_deleted"))
        val updated = accounts().map {
            if (it.id == account.id) it.copy(balance = candidate.balance, updatedAt = candidate.capturedAt,
                source = "account.source_xiaoxing") else it
        }
        prefs.edit().putString("accounts", accountsJson(updated).toString()).remove("candidate").apply()
    }
    private fun readArray(key: String): JSONArray = runCatching { JSONArray(prefs.getString(key, "[]")) }
        .getOrElse { JSONArray() }
    private fun put(key: String, value: String) { prefs.edit().putString(key, value).apply() }
    private fun writeAccounts(accounts: List<BankAccount>) = put("accounts", accountsJson(accounts).toString())
    private fun accountsJson(accounts: List<BankAccount>) = JSONArray().apply {
        accounts.forEach { put(JSONObject().put("id", it.id).put("name", it.name)
            .put("balance", it.balance).put("updatedAt", it.updatedAt).put("source", it.source)
            .put("createdAt", it.createdAt)) }
    }
    private fun goalJson(goal: SavingGoal) = JSONObject().put("id", goal.id).put("title", goal.title)
        .put("target", goal.target).put("accountIds", JSONArray(goal.accountIds.toList()))
        .put("mode", goal.mode.name).put("baseline", goal.baseline).put("deadline", goal.deadline)
        .put("colorIndex", goal.colorIndex).put("createdAt", goal.createdAt).put("archived", goal.archived).put("archivedAt", goal.archivedAt)
        .put("links", JSONArray().apply { goal.links.forEach { link ->
            put(JSONObject().put("accountId", link.accountId).put("mode", link.mode.name)
                .put("baseline", link.baseline).put("baselineAt", link.baselineAt))
        } })

    private fun linksFrom(o: JSONObject, accountIds: Set<String>): List<GoalAccountLink> {
        val array = o.optJSONArray("links") ?: return emptyList()
        return (0 until array.length()).mapNotNull { i ->
            runCatching {
                array.getJSONObject(i).let { link ->
                    GoalAccountLink(link.getString("accountId"),
                        runCatching { GoalMode.valueOf(link.optString("mode", "GROWTH")) }
                            .getOrDefault(GoalMode.GROWTH),
                        link.optLong("baseline"), link.optLong("baselineAt"))
                }
            }.getOrNull()
        }.filter { it.accountId in accountIds }.distinctBy { it.accountId }
    }
}
