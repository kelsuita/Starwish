package com.kelsuita.starswish.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.squircle.addSquircleRect
import top.yukonga.miuix.kmp.squircle.isSquircleEnabled
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.SinkFeedback
import top.yukonga.miuix.kmp.utils.TiltFeedback
import top.yukonga.miuix.kmp.utils.pressable

/**
 * Corner-tile size as a multiple of the corner radius, shared by every card and by the drag shadow
 * that has to follow the same silhouette.
 *
 * 1.0 draws a circular arc and 1.1 is Miuix's own default; larger values stretch the transition so
 * the curvature ramps in from the straight edge instead of jumping, which is the G2 continuous
 * corner look.
 */
const val CardCornerExtension = 1.5f

/**
 * The card silhouette: a G2 continuous corner built from the same path the drag shadow uses, so a
 * lifted card's shadow always tracks its own outline.
 *
 * Falls back to a plain rounded rectangle when runtime shaders are unavailable, matching Miuix.
 */
@Composable
fun cardCornerShape(cornerRadius: Dp): Shape {
    val radiusPx = with(LocalDensity.current) { cornerRadius.toPx() }
    val squircleEnabled = isSquircleEnabled()
    return remember(radiusPx, squircleEnabled, cornerRadius) {
        if (!squircleEnabled) {
            RoundedCornerShape(cornerRadius)
        } else {
            GenericShape { size, _ ->
                addSquircleRect(
                    size.width,
                    size.height,
                    radiusPx,
                    extension = CardCornerExtension,
                )
            }
        }
    }
}

/**
 * Drop-in replacement for Miuix's [top.yukonga.miuix.kmp.basic.Card] that draws the same colours,
 * margins, press feedback and click handling but with a G2 continuous corner.
 *
 * Miuix hard-codes its own corner extension inside the card and the drag shadow is path based, so
 * the silhouette is composed here from [cardCornerShape] to keep the two identical.
 */
@Composable
fun G2Card(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CardDefaults.CornerRadius,
    insideMargin: PaddingValues = CardDefaults.InsideMargin,
    colors: CardColors = CardDefaults.defaultColors(),
    pressFeedbackType: PressFeedbackType = PressFeedbackType.None,
    showIndication: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongPress by rememberUpdatedState(onLongPress)
    val hasOnLongPress = onLongPress != null

    val pressFeedback = remember(pressFeedbackType) {
        when (pressFeedbackType) {
            PressFeedbackType.None -> null
            PressFeedbackType.Sink -> SinkFeedback()
            PressFeedbackType.Tilt -> TiltFeedback()
        }
    }
    val usedInteractionSource = if (pressFeedback != null) interactionSource else null
    val indicationToUse = if (showIndication) LocalIndication.current else null
    val isClickable = onClick != null || hasOnLongPress
    val clickableModifier = remember(isClickable, hasOnLongPress, interactionSource, indicationToUse) {
        if (isClickable) {
            Modifier.combinedClickable(
                interactionSource = interactionSource,
                indication = indicationToUse,
                onClick = { currentOnClick?.invoke() },
                onLongClick = if (hasOnLongPress) {
                    { currentOnLongPress?.invoke() }
                } else {
                    null
                },
            )
        } else {
            Modifier
        }
    }
    val shape = cardCornerShape(cornerRadius)

    CompositionLocalProvider(LocalContentColor provides colors.contentColor) {
        Box(
            modifier = modifier
                .semantics(mergeDescendants = false) {
                    isTraversalGroup = true
                }
                .pressable(
                    interactionSource = usedInteractionSource,
                    indication = pressFeedback,
                    delay = null,
                )
                .clip(shape)
                .background(colors.color),
            propagateMinConstraints = true,
        ) {
            Column(
                modifier = Modifier
                    .then(clickableModifier)
                    .padding(insideMargin),
                content = content,
            )
        }
    }
}
