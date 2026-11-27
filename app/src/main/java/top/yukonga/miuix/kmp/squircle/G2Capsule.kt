// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.squircle

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape

/**
 * G2 (curvature-continuous) capsule, added on top of the vendored miuix-squircle code.
 *
 * A capsule has no straight run between its corners, so the usual "stretch the corner tile behind a
 * straight edge" trick has nothing to work with: `addSquircleRect` clamps the tile to half the short
 * side and the cap degenerates to whatever a single cubic gives. Instead each quarter cap is built
 * from **two** cubic segments whose curvature is exactly zero where the cap meets the straight edge
 * and continuous where the two segments meet on the 45° diagonal. The curvature therefore ramps
 * 0 → peak → 0 across the cap, which is what reads as a continuous (G2) transition.
 *
 * [FULLNESS] is where the curve crosses the 45° diagonal, in units of the cap radius; a circular cap
 * sits at `1 - 1/√2 ≈ 0.293`, so a smaller value pushes the silhouette closer to the box corner and
 * makes the cap fuller. [HANDLE] is the endpoint control offset that makes the endpoint curvature
 * vanish — the two are locked together by the geometry, see `G2CapsuleTest`.
 */
internal const val G2_CAPSULE_FULLNESS = 0.24f

internal const val G2_CAPSULE_HANDLE = 0.129f

/**
 * Control points of one quarter cap, laid out for the top-left corner of a `radius × radius` box:
 * `[A, P1, P2, M, P3, P4, B]` where A = (r, 0), B = (0, r) and M = (m, m) is the 45° join.
 *
 * Traversing A → M → B runs counter-clockwise on screen, which is why the top-left and bottom-right
 * corners of the outline are emitted backwards.
 */
internal fun g2CapCubics(
    radiusPx: Float,
    fullness: Float = G2_CAPSULE_FULLNESS,
    handle: Float = G2_CAPSULE_HANDLE,
): FloatArray {
    val r = radiusPx
    val m = r * fullness
    val h = r * handle
    return floatArrayOf(
        r, 0f,
        r - h, 0f,
        2f * m, 0f,
        m, m,
        0f, 2f * m,
        0f, r - h,
        0f, r,
    )
}

/**
 * Appends a full capsule outline: straight edges plus four [g2CapCubics] caps.
 *
 * The cap radius is always half the short side, so the two caps on that axis meet exactly.
 */
fun Path.addG2Capsule(width: Float, height: Float) {
    if (width <= 0f || height <= 0f) return
    val r = minOf(width, height) * 0.5f
    if (r <= 0f) {
        addRect(Rect(0f, 0f, width, height))
        return
    }
    val cap = g2CapCubics(r)
    moveTo(r, 0f)
    lineTo(width - r, 0f)
    capForward(cap, cornerX = width, cornerY = 0f, sx = -1f, sy = 1f)
    lineTo(width, height - r)
    capReversed(cap, cornerX = width, cornerY = height, sx = -1f, sy = -1f)
    lineTo(r, height)
    capForward(cap, cornerX = 0f, cornerY = height, sx = 1f, sy = -1f)
    lineTo(0f, r)
    capReversed(cap, cornerX = 0f, cornerY = 0f, sx = 1f, sy = 1f)
    close()
}

/** Emits the cap from its horizontal-edge endpoint to its vertical-edge endpoint. */
private fun Path.capForward(
    cap: FloatArray,
    cornerX: Float,
    cornerY: Float,
    sx: Float,
    sy: Float,
) {
    cubicTo(
        cornerX + sx * cap[2], cornerY + sy * cap[3],
        cornerX + sx * cap[4], cornerY + sy * cap[5],
        cornerX + sx * cap[6], cornerY + sy * cap[7],
    )
    cubicTo(
        cornerX + sx * cap[8], cornerY + sy * cap[9],
        cornerX + sx * cap[10], cornerY + sy * cap[11],
        cornerX + sx * cap[12], cornerY + sy * cap[13],
    )
}

/** Emits the same cap backwards, from its vertical-edge endpoint to its horizontal-edge endpoint. */
private fun Path.capReversed(
    cap: FloatArray,
    cornerX: Float,
    cornerY: Float,
    sx: Float,
    sy: Float,
) {
    cubicTo(
        cornerX + sx * cap[10], cornerY + sy * cap[11],
        cornerX + sx * cap[8], cornerY + sy * cap[9],
        cornerX + sx * cap[6], cornerY + sy * cap[7],
    )
    cubicTo(
        cornerX + sx * cap[4], cornerY + sy * cap[5],
        cornerX + sx * cap[2], cornerY + sy * cap[3],
        cornerX + sx * cap[0], cornerY + sy * cap[1],
    )
}

/** A [Shape] that draws [addG2Capsule] for whatever size it is laid out at. */
fun g2CapsuleShape(): Shape = GenericShape { size, _ ->
    addG2Capsule(size.width, size.height)
}

/**
 * True when a surface with [radiusPx] corners should be drawn as a [addG2Capsule] instead of the
 * squircle silhouette: the corner tile is clamped to half the short side (so there is no straight
 * run left) and the box is clearly wider than it is tall — that is a capsule, not a circle.
 */
internal fun isG2Capsule(
    size: Size,
    radiusPx: Float,
    extension: Float,
): Boolean {
    val halfMin = minOf(size.width, size.height) * 0.5f
    if (halfMin <= 0f) return false
    val ext = extension.coerceIn(SquircleDefaults.ExtensionMin, SquircleDefaults.ExtensionMax)
    if (radiusPx * ext < halfMin) return false
    val longSide = maxOf(size.width, size.height)
    val shortSide = minOf(size.width, size.height)
    return shortSide > 0f && longSide / shortSide > 1.2f
}
