package com.kelsuita.starswish.squircle

import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import top.yukonga.miuix.kmp.squircle.g2CapCubics
import top.yukonga.miuix.kmp.squircle.isG2Capsule
import kotlin.math.abs
import kotlin.math.hypot

/**
 * Pins the geometry of the G2 capsule cap: the whole point of the two-segment construction is that
 * the curvature vanishes where the cap meets the straight edge and stays continuous where the two
 * segments meet, so a change to the fullness / handle constants that breaks either property has to
 * fail here instead of shipping.
 *
 * Curvature is evaluated from the analytic Bézier derivatives — sampling the curve and differencing
 * neighbours loses too much precision right where the curvature approaches zero.
 */
class G2CapsuleTest {

    private val radius = 100f

    private fun capPoint(cap: FloatArray, second: Boolean, t: Float): Pair<Float, Float> {
        val base = if (second) 6 else 0
        val p0x = cap[base]; val p0y = cap[base + 1]
        val p1x = cap[base + 2]; val p1y = cap[base + 3]
        val p2x = cap[base + 4]; val p2y = cap[base + 5]
        val p3x = cap[base + 6]; val p3y = cap[base + 7]
        val u = 1f - t
        val x = u * u * u * p0x + 3f * u * u * t * p1x + 3f * u * t * t * p2x + t * t * t * p3x
        val y = u * u * u * p0y + 3f * u * u * t * p1y + 3f * u * t * t * p2y + t * t * t * p3y
        return x to y
    }

    /** Curvature of a cubic at [t], from the analytic first and second derivatives. */
    private fun curvatureAt(cap: FloatArray, second: Boolean, t: Float): Float {
        val base = if (second) 6 else 0
        val p = FloatArray(8) { cap[base + it] }
        val u = 1f - t
        val d1x = 3f * (u * u * (p[2] - p[0]) + 2f * u * t * (p[4] - p[2]) + t * t * (p[6] - p[4]))
        val d1y = 3f * (u * u * (p[3] - p[1]) + 2f * u * t * (p[5] - p[3]) + t * t * (p[7] - p[5]))
        val d2x = 6f * (u * (p[4] - 2f * p[2] + p[0]) + t * (p[6] - 2f * p[4] + p[2]))
        val d2y = 6f * (u * (p[5] - 2f * p[3] + p[1]) + t * (p[7] - 2f * p[5] + p[3]))
        val speed = hypot(d1x, d1y)
        if (speed <= 0f) return 0f
        val cross = abs(d1x * d2y - d1y * d2x)
        return cross / (speed * speed * speed)
    }

    @Test
    fun `curvature vanishes where the cap meets the straight edge`() {
        val cap = g2CapCubics(radius)
        assertEquals("start curvature", 0f, curvatureAt(cap, second = false, t = 0f), 1e-6f)
        assertEquals("end curvature", 0f, curvatureAt(cap, second = true, t = 1f), 1e-6f)
    }

    @Test
    fun `curvature is continuous at the 45 degree join`() {
        val cap = g2CapCubics(radius)
        val beforeJoin = curvatureAt(cap, second = false, t = 1f)
        val afterJoin = curvatureAt(cap, second = true, t = 0f)
        assertTrue("join curvature $beforeJoin should be positive", beforeJoin > 0f)
        assertEquals("curvature jumped at the join", beforeJoin, afterJoin, beforeJoin * 0.001f)
        // A capsule still has to turn 90 degrees per cap, so the peak must exceed a circle's.
        assertTrue("peak $beforeJoin should exceed 1/r = ${1f / radius}", beforeJoin > 1f / radius)
    }

    @Test
    fun `cap is monotone inside its box and starts on the edge`() {        val cap = g2CapCubics(radius)
        val points = ArrayList<Pair<Float, Float>>()
        val steps = 500
        for (i in 0..steps) points.add(capPoint(cap, second = false, t = i.toFloat() / steps))
        for (i in 1..steps) points.add(capPoint(cap, second = true, t = i.toFloat() / steps))

        assertEquals(radius, points.first().first, 0.01f)
        assertEquals(0f, points.first().second, 0.01f)
        assertEquals(0f, points.last().first, 0.01f)
        assertEquals(radius, points.last().second, 0.01f)

        for (i in 1 until points.size) {
            assertTrue("x went back at $i", points[i].first <= points[i - 1].first + 1e-3f)
            assertTrue("y went back at $i", points[i].second >= points[i - 1].second - 1e-3f)
        }
        for ((x, y) in points) {
            assertTrue("x out of box: $x", x >= -1e-3f && x <= radius + 1e-3f)
            assertTrue("y out of box: $y", y >= -1e-3f && y <= radius + 1e-3f)
        }
    }

    @Test
    fun `only wide clamped surfaces route to the capsule path`() {
        // The floating bottom bar: 1008 x 117 px with a 50dp (112.5 px) radius is a capsule.
        assertTrue(isG2Capsule(Size(1008f, 117f), radiusPx = 112.5f, extension = 1.5f))
        // Same height, but a radius that still leaves a straight run: ordinary squircle.
        assertFalse(isG2Capsule(Size(1008f, 117f), radiusPx = 16f, extension = 1.5f))
        // A square icon button with a capsule-sized radius is a circle, not a capsule.
        assertFalse(isG2Capsule(Size(90f, 90f), radiusPx = 90f, extension = 1.5f))
        // A card is never a capsule.
        assertFalse(isG2Capsule(Size(900f, 340f), radiusPx = 36f, extension = 1.5f))
        // Nothing to draw.
        assertFalse(isG2Capsule(Size(0f, 0f), radiusPx = 40f, extension = 1.5f))
    }
}
