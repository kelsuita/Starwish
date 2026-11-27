package com.kelsuita.starswish.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.basic.ButtonColors
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
 * Solid accent [ButtonColors] (the 通过WebDAV读取 style action).
 *
 * Same rule as [primaryTextButtonColors]: on the stock palette in dark mode the fill becomes
 * `primaryContainer` so it sits on the dark page without the heavier `primary`.
 */
@Composable
fun primaryButtonColors(): ButtonColors =
    if (LocalStockPalette.current && isDarkScheme()) {
        ButtonDefaults.buttonColors(
            color = MiuixTheme.colorScheme.primaryContainer,
            contentColor = MiuixTheme.colorScheme.onPrimaryContainer,
        )
    } else {
        ButtonDefaults.buttonColorsPrimary()
    }

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
