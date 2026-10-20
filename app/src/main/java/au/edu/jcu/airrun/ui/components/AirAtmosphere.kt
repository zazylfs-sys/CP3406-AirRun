package au.edu.jcu.airrun.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** What the sky behind the verdict looks like. */
enum class AtmosphereStyle {
    /** A few drifting motes and a soft sun glow. */
    CLEAR,
    /** Dense specks and slow smog bands. */
    HAZE,
    /** Slanted falling streaks. */
    RAIN,
}

/** Normalised (0..1) particle placement, so the same set fits any card size. */
private data class Particle(
    val x: Float,
    val y: Float,
    val radiusDp: Float,
    val alpha: Float,
    val speed: Float,
    val phase: Float,
)

/**
 * Animated background for the verdict card. The number of particles grows with
 * PM2.5, so dirtier air *looks* dirtier, a reading you take in at a glance.
 *
 * The animation clock is read only inside the draw lambda, so each frame redraws
 * without recomposing.
 */
@Composable
fun AirAtmosphere(
    style: AtmosphereStyle,
    particleCount: Int,
    particleColor: Color,
    glowColor: Color,
    hazeColor: Color,
    modifier: Modifier = Modifier,
) {
    val particles = remember(particleCount) {
        val random = Random(seed = 7)
        List(particleCount) {
            Particle(
                x = random.nextFloat(),
                y = random.nextFloat(),
                radiusDp = 1.5f + random.nextFloat() * 2.5f,
                alpha = 0.25f + random.nextFloat() * 0.45f,
                speed = 0.6f + random.nextFloat() * 0.8f,
                phase = random.nextFloat() * 2f * PI.toFloat(),
            )
        }
    }
    val time = animationClock()

    Canvas(modifier) {
        val t = time.value
        when (style) {
            AtmosphereStyle.CLEAR -> drawSunGlow(glowColor, t)
            AtmosphereStyle.HAZE -> drawHazeBands(hazeColor, t)
            AtmosphereStyle.RAIN -> Unit
        }
        particles.forEach { p ->
            if (style == AtmosphereStyle.RAIN) drawRainStreak(p, particleColor, t) else drawMote(p, particleColor, t)
        }
    }
}

/** A looping 0..1 clock, or a fixed frame when animations are off. */
@Composable
private fun animationClock(): State<Float> {
    if (!rememberAnimationsEnabled()) return remember { mutableFloatStateOf(0.3f) }
    return rememberInfiniteTransition(label = "atmosphere").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 12_000, easing = LinearEasing), RepeatMode.Restart),
        label = "clock",
    )
}

private fun DrawScope.drawMote(p: Particle, color: Color, t: Float) {
    val angle = t * 2f * PI.toFloat() * p.speed + p.phase
    val center = Offset(
        x = p.x * size.width + sin(angle) * 8.dp.toPx(),
        y = p.y * size.height + cos(angle) * 14.dp.toPx(),
    )
    drawCircle(color = color, radius = p.radiusDp.dp.toPx(), center = center, alpha = p.alpha)
}

private fun DrawScope.drawRainStreak(p: Particle, color: Color, t: Float) {
    val length = 10.dp.toPx()
    // Each streak falls the full height once per loop at its own speed, then wraps around.
    val y = ((p.y + t * p.speed * 3f) % 1f) * (size.height + length) - length
    val x = p.x * size.width - (y / size.height) * 12.dp.toPx()
    drawLine(
        color = color.copy(alpha = p.alpha),
        start = Offset(x, y),
        end = Offset(x - 3.dp.toPx(), y + length),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawSunGlow(color: Color, t: Float) {
    val pulse = 1f + 0.07f * sin(t * 4f * PI.toFloat())
    val radius = 120.dp.toPx() * pulse
    val center = Offset(size.width - 40.dp.toPx(), 30.dp.toPx())
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.6f), color.copy(alpha = 0f)),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

private fun DrawScope.drawHazeBands(color: Color, t: Float) {
    val drift = sin(t * 2f * PI.toFloat()) * 28.dp.toPx()
    listOf(0.06f to 1f, 0.42f to -1f, 0.78f to 1f).forEach { (top, direction) ->
        val height = 80.dp.toPx()
        val y = top * size.height
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, color.copy(alpha = 0.22f), Color.Transparent),
                startY = y,
                endY = y + height,
            ),
            topLeft = Offset(-size.width * 0.2f + drift * direction, y),
            size = Size(size.width * 1.4f, height),
        )
    }
}
