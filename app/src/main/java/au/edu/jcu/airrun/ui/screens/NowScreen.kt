package au.edu.jcu.airrun.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.edu.jcu.airrun.R
import au.edu.jcu.airrun.data.model.RunnerProfile
import au.edu.jcu.airrun.domain.Factor
import au.edu.jcu.airrun.domain.Pm25Band
import au.edu.jcu.airrun.domain.PsiCategory
import au.edu.jcu.airrun.domain.RainOutlook
import au.edu.jcu.airrun.domain.UvCategory
import au.edu.jcu.airrun.ui.NowDashboard
import au.edu.jcu.airrun.ui.NowUiState
import au.edu.jcu.airrun.ui.PreviewData
import au.edu.jcu.airrun.ui.components.ErrorView
import au.edu.jcu.airrun.ui.components.ForecastValue
import au.edu.jcu.airrun.ui.components.LoadingView
import au.edu.jcu.airrun.ui.components.MetricNumber
import au.edu.jcu.airrun.ui.components.MetricTile
import au.edu.jcu.airrun.ui.components.VerdictCard
import au.edu.jcu.airrun.ui.formatClockTime
import au.edu.jcu.airrun.ui.level
import au.edu.jcu.airrun.ui.regionPlaceName
import au.edu.jcu.airrun.ui.theme.AirRunTheme

/** The main utility screen: can I run outside right now, and why. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowScreen(
    uiState: NowUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        NowUiState.Loading -> LoadingView(modifier)
        NowUiState.Error -> ErrorView(onRetry = onRefresh, modifier = modifier)
        is NowUiState.Ready -> PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier.fillMaxSize(),
        ) {
            NowContent(
                dashboard = uiState.dashboard,
                isRefreshing = uiState.isRefreshing,
                refreshFailed = uiState.refreshFailed,
                onRefresh = onRefresh,
            )
        }
    }
}

@Composable
private fun NowContent(
    dashboard: NowDashboard,
    isRefreshing: Boolean,
    refreshFailed: Boolean,
    onRefresh: () -> Unit,
) {
    val measuredAt = dashboard.conditions.measuredAt?.let { formatClockTime(it) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
    ) {
        NowHeader(
            place = regionPlaceName(dashboard.conditions.region),
            measuredAt = measuredAt,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
        )
        if (refreshFailed && !isRefreshing) {
            StaleNotice(measuredAt)
        }
        VerdictCard(dashboard = dashboard, modifier = Modifier.padding(horizontal = 16.dp))
        ReadingsGrid(dashboard = dashboard, modifier = Modifier.padding(16.dp))
        Text(
            text = stringResource(
                if (dashboard.settings.profile == RunnerProfile.SENSITIVE) R.string.footer_sensitive
                else R.string.footer_general
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun NowHeader(place: String, measuredAt: String?, isRefreshing: Boolean, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(painterResource(R.drawable.ic_place), contentDescription = null, modifier = Modifier.size(18.dp))
                Text(place, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            }
            if (measuredAt != null) {
                Text(
                    text = stringResource(R.string.updated_at, measuredAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        RefreshButton(isRefreshing = isRefreshing, onRefresh = onRefresh)
    }
}

/** Outlined refresh button whose icon spins while a refresh is running. */
@Composable
private fun RefreshButton(isRefreshing: Boolean, onRefresh: () -> Unit) {
    val spin = rememberInfiniteTransition(label = "refresh").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "spin",
    )
    OutlinedIconButton(
        onClick = onRefresh,
        enabled = !isRefreshing,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.size(44.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_refresh),
            contentDescription = stringResource(R.string.refresh),
            modifier = Modifier.rotate(if (isRefreshing) spin.value else 0f),
        )
    }
}

@Composable
private fun StaleNotice(measuredAt: String?) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
    ) {
        Text(
            text = if (measuredAt != null) stringResource(R.string.refresh_failed, measuredAt)
            else stringResource(R.string.refresh_failed_no_time),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

/** The four supporting readings in a 2 × 2 grid of equal-height tiles. */
@Composable
private fun ReadingsGrid(dashboard: NowDashboard, modifier: Modifier = Modifier) {
    val c = dashboard.conditions
    val settings = dashboard.settings
    val deciding = dashboard.advice.decidingFactor
    val noData = stringResource(R.string.no_data)
    val excluded = stringResource(R.string.not_in_verdict)

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TileRow(
            left = { m ->
                val level = c.psi24Hour?.let { PsiCategory.of(it).level }
                MetricTile(
                    caption = stringResource(R.string.tile_psi),
                    level = level,
                    status = level?.let { stringResource(it.label) } ?: noData,
                    highlighted = deciding == Factor.PSI,
                    modifier = m,
                ) { MetricNumber(c.psi24Hour?.toString() ?: "–") }
            },
            right = { m ->
                val level = c.pm25OneHour?.let { Pm25Band.of(it).level }
                MetricTile(
                    caption = stringResource(R.string.tile_pm25),
                    level = level,
                    status = level?.let { stringResource(it.label) } ?: noData,
                    highlighted = deciding == Factor.PM25,
                    modifier = m,
                ) { MetricNumber(c.pm25OneHour?.toString() ?: "–", stringResource(R.string.unit_pm25)) }
            },
        )
        TileRow(
            left = { m ->
                val level = c.uvIndex?.let { UvCategory.of(it).level }
                MetricTile(
                    caption = stringResource(R.string.tile_uv),
                    level = level,
                    status = when {
                        !settings.includeUv -> excluded
                        level != null -> stringResource(level.label)
                        else -> noData
                    },
                    highlighted = deciding == Factor.UV,
                    enabled = settings.includeUv,
                    modifier = m,
                ) { MetricNumber(c.uvIndex?.toString() ?: "–") }
            },
            right = { m ->
                val level = c.forecast?.let { RainOutlook.of(it).level }
                MetricTile(
                    caption = stringResource(R.string.tile_forecast),
                    level = level,
                    status = when {
                        !settings.includeRain -> excluded
                        level != null -> stringResource(level.label)
                        else -> noData
                    },
                    highlighted = deciding == Factor.RAIN,
                    enabled = settings.includeRain,
                    modifier = m,
                ) {
                    if (c.forecast != null) ForecastValue(c.forecast) else MetricNumber("–")
                }
            },
        )
    }
}

@Composable
private fun TileRow(
    left: @Composable (Modifier) -> Unit,
    right: @Composable (Modifier) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val cell = Modifier
            .weight(1f)
            .fillMaxHeight()
        left(cell)
        right(cell)
    }
}

@Preview(name = "Clear air", showBackground = true, heightDp = 760)
@Composable
private fun NowScreenClearPreview() {
    AirRunTheme(darkTheme = false) {
        NowScreen(uiState = NowUiState.Ready(PreviewData.clearDay), onRefresh = {})
    }
}

@Preview(name = "Haze, sensitive (dark)", showBackground = true, heightDp = 760, backgroundColor = 0xFF0F1513)
@Composable
private fun NowScreenHazePreview() {
    AirRunTheme(darkTheme = true) {
        NowScreen(uiState = NowUiState.Ready(PreviewData.hazeDaySensitive), onRefresh = {})
    }
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun NowScreenErrorPreview() {
    AirRunTheme { NowScreen(uiState = NowUiState.Error, onRefresh = {}) }
}
