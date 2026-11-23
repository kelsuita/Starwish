package com.kelsuita.starswish.data

import android.accounts.AccountManager
import android.content.Context

/**
 * Best-effort name of the phone's signed-in account, used as the title of the "add everything up"
 * banner. Android 8 and later only expose accounts this app is allowed to see, so this can return
 * null on devices that keep their system account private; the UI then falls back to a generic
 * label instead of showing a wrong name.
 */
object DeviceAccountName {

    /** Vendors whose system account is the one the user thinks of as "my phone account". */
    private val preferredTypes = listOf(
        "com.google",
        "com.oneplus",
        "com.oplus",
        "com.oppo",
        "com.coloros",
        "com.xiaomi",
        "com.miui",
        "com.samsung.android",
        "com.samsung",
        "com.huawei",
        "com.hihonor",
        "com.vivo",
        "com.transsion",
    )

    fun resolve(context: Context): String? = runCatching {
        val accounts = AccountManager.get(context).accounts.orEmpty()
        val account = accounts.firstOrNull { it.type in preferredTypes } ?: accounts.firstOrNull()
        account?.name?.trim()?.takeIf { it.isNotEmpty() }
    }.getOrNull()
}
