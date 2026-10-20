package au.edu.jcu.airrun.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import au.edu.jcu.airrun.R
import au.edu.jcu.airrun.domain.Factor
import au.edu.jcu.airrun.domain.RiskLevel
import au.edu.jcu.airrun.ui.NowDashboard
import au.edu.jcu.airrun.ui.decidingFactorText
import au.edu.jcu.airrun.ui.decidingFactorTone
import au.edu.jcu.airrun.ui.theme.AirRunTheme
import au.edu.jcu.airrun.ui.verdictBody
import au.edu.jcu.airrun.ui.verdictLabel
import au.edu.jcu.airrun.ui.verdictTitle

/** The hero card: one big answer, why, and the factor that decided it. */
@Composable
fun VerdictCard(dashboard: NowDashboard, modifier: Modifier = Modifier) {
    val verdict = dashboard.advice.verdict
    val palette = AirRunTheme.colors.verdict(verdict)
    val container by animateColorAsState(palette.container, label = "container")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(container)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
    ) {
        AirAtmosphere(
            style = atmosphereFor(dashboard),
            particleCount = particleCountFor(dashboard.conditions.pm25OneHour),
            particleColor = palette.particle,
            glowColor = AirRunTheme.colors.sunGlow,
            hazeColor = AirRunTheme.colors.haze,
            modifier = Modifier.matchParentSize(),
        )
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = verdictLabel(dashboard),
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.label,
                    modifier = Modifier.weight(1f),
                )
                VerdictBadge(verdict = verdict, color = palette.badge, iconColor = palette.onBadge)
            }

            AnimatedContent(
                targetState = verdictTitle(verdict, dashboard.settings.intensity),
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 3 }) togetherWith fadeOut()
                },
                label = "verdictTitle",
            ) { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.displayMedium,
                    color = palette.title,
                    modifier = Modifier.semantics { heading() },
                )
            }

            Text(
                text = verdictBody(dashboard),
                style = MaterialTheme.typography.bodyLarge,
                color = palette.body,
            )

            decidingFactorText(dashboard)?.let { factorText ->
                HorizontalDivider(color = palette.divider)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.deciding_factor),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.label,
                        modifier = Modifier.weight(1f),
                    )
                    FactorChip(
                        text = factorText,
                        dotColor = AirRunTheme.colors.bar(decidingFactorTone(dashboard)),
                        container = palette.chip,
                        content = palette.onChip,
                    )
                }
            }
        }
    }
}

@Composable
private fun FactorChip(text: String, dotColor: Color, container: Color, content: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

/** A round status icon with a "sonar" ring that pulses faster when the news is worse. */
@Composable
private fun VerdictBadge(verdict: RiskLevel, color: Color, iconColor: Color) {
    val periodMillis = if (verdict == RiskLevel.OK) 2_400 else 1_600
    val animate = rememberAnimationsEnabled()
    val ring = rememberInfiniteTransition(label = "badge").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis), RepeatMode.Restart),
        label = "ring",
    )
    Box(
        modifier = Modifier
            .size(36.dp)
            .drawBehind {
                if (!animate) return@drawBehind
                val progress = ring.value
                drawCircle(
                    color = color,
                    radius = size.minDimension / 2 + 14.dp.toPx() * progress,
                    alpha = 0.45f * (1f - progress),
                )
            }
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(
                when (verdict) {
                    RiskLevel.OK -> R.drawable.ic_check
                    RiskLevel.CAUTION -> R.drawable.ic_minus
                    RiskLevel.AVOID -> R.drawable.ic_alert
                }
            ),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun atmosphereFor(dashboard: NowDashboard): AtmosphereStyle {
    val advice = dashboard.advice
    return when {
        advice.verdict == RiskLevel.OK -> AtmosphereStyle.CLEAR
        advice.decidingFactor == Factor.RAIN -> AtmosphereStyle.RAIN
        else -> AtmosphereStyle.HAZE
    }
}

/** About one speck per 3 µg/m³: a handful on a clean day, a cloud on a haze day. */
private fun particleCountFor(pm25: Int?): Int = ((pm25 ?: 20) / 3).coerceIn(10, 70)
