package au.edu.jcu.airrun.data

import au.edu.jcu.airrun.data.model.Region
import au.edu.jcu.airrun.data.repository.AirDataUnavailableException
import au.edu.jcu.airrun.data.repository.NetworkAirQualityRepository
import au.edu.jcu.airrun.fakes.FakeNeaApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class NetworkAirQualityRepositoryTest {

    private fun TestScope.repository(api: FakeNeaApi) =
        NetworkAirQualityRepository(api, ioDispatcher = StandardTestDispatcher(testScheduler))

    @Test
    fun allFeedsUp_combinesEverything() = runTest {
        val repo = repository(FakeNeaApi())

        val readings = repo.fetchReadings()

        assertEquals(48, readings.psi24Hour[Region.WEST])
        assertEquals(22, readings.pm25OneHour[Region.WEST])
        assertEquals(6, readings.uvIndex)
        assertEquals(10, readings.airMeasuredAt?.hour)
    }

    @Test
    fun optionalFeedsDown_stillReturnsAirQuality() = runTest {
        val repo = repository(FakeNeaApi(uvFails = true, forecastFails = true))

        val readings = repo.fetchReadings()

        assertEquals(48, readings.psi24Hour[Region.WEST])
        assertNull(readings.uvIndex)
        assertNull(readings.forecast)
    }

    @Test
    fun oneAirFeedDown_usesTheOther() = runTest {
        val repo = repository(FakeNeaApi(psiFails = true))

        val readings = repo.fetchReadings()

        assertTrue(readings.psi24Hour.isEmpty())
        assertEquals(22, readings.pm25OneHour[Region.WEST])
    }

    @Test
    fun bothAirFeedsDown_throws() = runTest {
        val repo = repository(FakeNeaApi(psiFails = true, pm25Fails = true))

        try {
            repo.fetchReadings()
            fail("Expected AirDataUnavailableException")
        } catch (e: AirDataUnavailableException) {
            // Coroutine stack-trace recovery may wrap the exception, so search the cause chain.
            val causes = generateSequence<Throwable>(e) { it.cause }
            assertTrue(causes.any { it.message == "psi down" })
        }
    }
}
