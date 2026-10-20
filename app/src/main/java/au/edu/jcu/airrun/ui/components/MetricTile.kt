package au.edu.jcu.airrun.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import au.edu.jcu.airrun.R
import au.edu.jcu.airrun.ui.ReadingLevel
import au.edu.jcu.airrun.ui.theme.AirRunTheme

/**
 * One reading on the Now screen: a caption, a big value, a 4-segment level bar and a
 * status line. [value] is a slot, so the same tile shows a number or an icon plus text.
 *
 * @param highlighted draws a coloured border on the factor that decided the verdict.
 * @param enabled false greys the tile out when the factor is excluded in Settings.
 */
@Composable
fun MetricTile(
    caption: String,
    level: ReadingLevel?,
    status: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    enabled: Boolean = true,
    value: @Composable () -> Unit,
) {
    val colors = AirRunTheme.colors
    val accent = level?.let { colors.bar(it.tone) } ?: colors.track
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (highlighted) 1.5.dp else 1.dp,
            color = if (highlighted) accent else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
                .alpha(if (enabled) 1f else 0.45f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(Modifier.heightIn(min = 48.dp), contentAlignment = Alignment.CenterStart) { value() }
            LevelBar(
                filledSegments = if (enabled) level?.segments ?: 0 else 0,
                color = accent,
                trackColor = colors.track,
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled && level != null) colors.text(level.tone) else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** A big number with an optional unit, e.g. "22 µg/m³". */
@Composable
fun MetricNumber(number: String, unit: String? = null) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(number, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface)
        if (unit != null) {
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
    }
}

/** A weather icon beside short forecast text, e.g. a cloud and "Partly Cloudy". */
@Composable
fun ForecastValue(forecast: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            painter = painterResource(weatherIcon(forecast)),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(30.dp),
        )
        Text(
            // "(Day)" / "(Night)" adds nothing in a 2-hour window, so drop it.
            text = forecast.replace(Regex("""\s*\((Day|Night)\)"""), ""),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Four rounded segments that grow in from the left when first shown. */
@Composable
fun LevelBar(filledSegments: Int, color: Color, trackColor: Color, modifier: Modifier = Modifier) {
    val animate = rememberAnimationsEnabled()
    val growth = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) {
        growth.animateTo(1f, tween(durationMillis = 700, delayMillis = 250, easing = FastOutSlowInEasing))
    }
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(SEGMENTS) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(trackColor)
            ) {
                if (index < filledSegments) {
                    Box(
                        Modifier
                            .matchParentSize()
                            .graphicsLayer {
                                scaleX = growth.value
                                transformOrigin = TransformOrigin(0f, 0.5f)
                            }
                            .background(color)
                    )
                }
            }
        }
    }
}

private const val SEGMENTS = 4

/** Picks an icon from NEA's forecast wording. */
@DrawableRes
fun weatherIcon(forecast: String): Int {
    val text = forecast.lowercase()
    return when {
        "thunder" in text -> R.drawable.ic_thunder
        "rain" in text || "shower" in text || "drizzle" in text -> R.drawable.ic_rain
        "haz" in text || "mist" in text || "fog" in text -> R.drawable.ic_haze
        "wind" in text -> R.drawable.ic_air
        "partly" in text -> R.drawable.ic_partly_cloudy
        "cloud" in text || "overcast" in text -> R.drawable.ic_cloud
        "night" in text -> R.drawable.ic_moon
        else -> R.drawable.ic_sun
    }
}
