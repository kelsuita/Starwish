package com.kelsuita.starswish.data

import kotlinx.serialization.Serializable

/** Theme 0 follows system; banner 0 is light, 1 is dark. */
@Serializable
data class Appearance(
    val themeMode: Int = 0,
    val accentIndex: Int = 0,
    val bannerStyle: Int = 0,
    val featuredGoalId: String = "",
    val paletteStyle: Int = 0,
    val colorSpec: Int = 1,
    val blurEnabled: Boolean = false,
    val pureBlackBackground: Boolean = false,
    val floatingNavBar: Boolean = false,
    /** 0 is the Miuix floating bar, 1 is the iOS-like floating bar. */
    val floatingNavBarStyle: Int = 0,
    /** Use the platform predictive-back animation for the tab pager. */
    val predictiveBackEnabled: Boolean = true,
    /** Decimal places shown on the home banner progress, 0 means a whole percent. */
    val bannerDecimals: Int = 0,
    /** Home banner shows every wish added up instead of one featured wish. */
    val bannerAllGoals: Boolean = false,
    /** Goal cards fold 更新余额 / 编辑 / 删除 into a corner menu instead of showing them inline. */
    val collapseCardActions: Boolean = false,
)
