package com.kelsuita.starswish.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButtonColors
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * True while the stock accent palette is active. Role overrides that only make sense for Miuix's
 * own colours are gated on this; wallpaper and fixed accents keep the roles generated for them.
 */
internal val LocalStockPalette = staticCompositionLocalOf { true }

/** True while the theme resolves to the dark colour scheme. */
@Composable
internal fun isDarkScheme(): Boolean = MiuixTheme.colorScheme.background.luminance() < 0.5f

/**
 * Solid accent [TextButtonColors] (the 更新余额 / 读取余额 style action).
 *
 * On the stock palette in dark mode the fill moves from `primary` to `primaryContainer`: the
 * container tint stays legible on the dark page while `onPrimaryContainer` keeps the label white.
 */
@Composable
fun primaryTextButtonColors(): TextButtonColors =
    if (LocalStockPalette.current && isDarkScheme()) {
        ButtonDefaults.textButtonColors(
            color = MiuixTheme.colorScheme.primaryContainer,
            textColor = MiuixTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        ButtonDefaults.textButtonColorsPrimary()
    }
