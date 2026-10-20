package au.edu.jcu.airrun.ui

import au.edu.jcu.airrun.data.model.RunnerProfile
import au.edu.jcu.airrun.data.repository.SettingsRepository
import au.edu.jcu.airrun.domain.Factor
import au.edu.jcu.airrun.domain.RiskLevel
import au.edu.jcu.airrun.fakes.FakeAirQualityRepository
import au.edu.jcu.airrun.fakes.TestReadings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AirRunViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    /** A clock the test can move forward. */
    private class MutableClock(var now: Instant) : Clock() {
        override fun instant(): Instant = now
        override fun getZone(): ZoneId = ZoneId.of("Asia/Singapore")
        override fun withZone(zone: ZoneId?): Clock = this
    }

    private val clock = MutableClock(Instant.parse("2026-10-14T02:00:00Z"))

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.createViewModel(repo: FakeAirQualityRepository): AirRunViewModel {
        val viewModel = AirRunViewModel(repo, SettingsRepository(), clock)
        // nowUiState uses WhileSubscribed, so keep a collector running for the test.
        backgroundScope.launch(dispatcher) { viewModel.nowUiState.collect() }
        return viewModel
    }

    private fun AirRunViewModel.ready(): NowUiState.Ready = nowUiState.value as NowUiState.Ready

    @Test
    fun successfulLoad_showsVerdict() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeAirQualityRepository(TestReadings.clear))

        val state = viewModel.ready()
        assertEquals(RiskLevel.OK, state.dashboard.advice.verdict)
        assertEquals("Jurong West", state.dashboard.conditions.forecastArea)
        assertFalse(state.isRefreshing)
        assertFalse(state.refreshFailed)
    }

    @Test
    fun failedFirstLoad_showsError_andRetryRecovers() = runTest(dispatcher) {
        val repo = FakeAirQualityRepository(
            listOf(Result.failure(IOException("offline")), Result.success(TestReadings.clear))
        )
        val viewModel = createViewModel(repo)
        assertEquals(NowUiState.Error, viewModel.nowUiState.value)

        viewModel.refresh()

        assertEquals(RiskLevel.OK, viewModel.ready().dashboard.advice.verdict)
    }

    @Test
    fun failedRefresh_keepsOldDataAndFlagsIt() = runTest(dispatcher) {
        val repo = FakeAirQualityRepository(
            listOf(Result.success(TestReadings.clear), Result.failure(IOException("offline")))
        )
        val viewModel = createViewModel(repo)

        viewModel.refresh()

        val state = viewModel.ready()
        assertTrue(state.refreshFailed)
        assertEquals(48, state.dashboard.conditions.psi24Hour)
    }

    @Test
    fun changingProfile_updatesVerdictWithoutRefetching() = runTest(dispatcher) {
        val repo = FakeAirQualityRepository(TestReadings.haze)
        val viewModel = createViewModel(repo)
        assertEquals(RiskLevel.CAUTION, viewModel.ready().dashboard.advice.verdict)

        viewModel.selectProfile(RunnerProfile.SENSITIVE)

        val advice = viewModel.ready().dashboard.advice
        assertEquals(RiskLevel.AVOID, advice.verdict)
        assertEquals(Factor.PM25, advice.decidingFactor)
        assertEquals(1, repo.calls)
    }

    @Test
    fun turningOffUv_removesItFromTheVerdict() = runTest(dispatcher) {
        val viewModel = createViewModel(FakeAirQualityRepository(TestReadings.clear))
        assertEquals(Factor.UV, viewModel.ready().dashboard.advice.decidingFactor)

        viewModel.setIncludeUv(false)

        assertTrue(viewModel.ready().dashboard.advice.assessments.none { it.factor == Factor.UV })
    }

    @Test
    fun refreshIfStale_onlyFetchesAfterFifteenMinutes() = runTest(dispatcher) {
        val repo = FakeAirQualityRepository(TestReadings.clear)
        val viewModel = createViewModel(repo)
        assertEquals(1, repo.calls)

        clock.now = clock.now.plus(Duration.ofMinutes(5))
        viewModel.refreshIfStale()
        assertEquals(1, repo.calls)

        clock.now = clock.now.plus(Duration.ofMinutes(10))
        viewModel.refreshIfStale()
        assertEquals(2, repo.calls)
    }
}
