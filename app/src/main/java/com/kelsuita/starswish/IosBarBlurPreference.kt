package com.kelsuita.starswish

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kelsuita.starswish.data.IOS_BAR_BLUR_DEFAULT_PX
import com.kelsuita.starswish.data.IOS_BAR_BLUR_MAX_PX
import com.kelsuita.starswish.data.tr
import com.kelsuita.starswish.ui.G2Button
import com.kelsuita.starswish.ui.G2TextButton
import com.kelsuita.starswish.ui.G2Card
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SliderPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
internal fun IosBarBlurPreference(
    enabled: Boolean,
    radius: Int,
    onRadiusChange: (Int) -> Unit,
    onRadiusChangeFinished: () -> Unit,
) {
    val title = tr("settings.ios_bar_blur")
    val valueText = "$radius/${IOS_BAR_BLUR_MAX_PX}f"
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var holdDown by rememberSaveable { mutableStateOf(false) }
    var input by rememberSaveable { mutableStateOf("") }

    G2Card(Modifier.fillMaxWidth()) {
        if (enabled) {
            SliderPreference(
                title = title,
                summary = tr("settings.ios_bar_blur_summary"),
                valueText = valueText,
                value = radius.toFloat(),
                valueRange = 0f..IOS_BAR_BLUR_MAX_PX.toFloat(),
                steps = IOS_BAR_BLUR_MAX_PX - 1,
                onValueChange = { onRadiusChange(it.roundToInt()) },
                onValueChangeFinished = onRadiusChangeFinished,
                onClick = {
                    input = radius.toString()
                    holdDown = true
                    showDialog = true
                },
                holdDownState = holdDown,
            )
        } else {
            ArrowPreference(
                title = title,
                summary = tr("settings.ios_bar_blur_unavailable"),
                enabled = false,
                endActions = {
                    Text(
                        valueText,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.disabledOnSecondaryVariant,
                    )
                },
            )
        }
    }
    WindowDialog(
        show = enabled && showDialog,
        title = tr("settings.ios_bar_blur_adjust"),
        summary = tr("settings.ios_bar_blur_input_hint"),
        onDismissRequest = { showDialog = false },
        onDismissFinished = { holdDown = false },
    ) {
        TextField(
            value = input,
            onValueChange = { text ->
                val digits = text.mapNotNull { it.digitToIntOrNull() }.joinToString("")
                input = if (digits.isEmpty()) "" else {
                    (digits.toIntOrNull() ?: IOS_BAR_BLUR_MAX_PX)
                        .coerceAtMost(IOS_BAR_BLUR_MAX_PX).toString()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Row(horizontalArrangement = Arrangement.SpaceBetween) {
            G2TextButton(
                text = tr("common.cancel"),
                onClick = { showDialog = false },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(20.dp))
            G2TextButton(
                text = tr("common.confirm"),
                onClick = {
                    onRadiusChange(input.toIntOrNull() ?: IOS_BAR_BLUR_DEFAULT_PX)
                    onRadiusChangeFinished()
                    showDialog = false
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    }
}
