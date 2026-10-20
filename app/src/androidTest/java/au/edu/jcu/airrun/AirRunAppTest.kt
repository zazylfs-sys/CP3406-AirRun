package au.edu.jcu.airrun

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.jcu.airrun.data.model.AirReadings
import au.edu.jcu.airrun.data.model.AreaForecast
import au.edu.jcu.airrun.data.model.GeoPoint
import au.edu.jcu.airrun.data.model.Region
import au.edu.jcu.airrun.data.model.TwoHourForecast
import au.edu.jcu.airrun.data.repository.AirQualityRepository
import au.edu.jcu.airrun.data.repository.SettingsRepository
import au.edu.jcu.airrun.ui.AirRunViewModel
import au.edu.jcu.airrun.ui.theme.AirRunTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** End-to-end UI test: the whole app with a fake repository instead of the network. */
@RunWith(AndroidJUnit4::class)
class AirRunAppTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Unhealthy PSI and Band II PM2.5 in the west; clean air in the north. */
    private val readings = AirReadings(
        psi24Hour = mapOf(Region.WEST to 142, Region.NORTH to 40),
        pm25OneHour = mapOf(Region.WEST to 100, Region.NORTH to 18),
        airMeasuredAt = null,
        uvIndex = 2,
        forecast = TwoHourForecast(
            validPeriod = "",
            areas = listOf(
                AreaForecast("Jurong West", GeoPoint(1.34039, 103.705), "Cloudy"),
                AreaForecast("Mandai", GeoPoint(1.419, 103.812), "Fair (Day)"),
            ),
        ),
    )

    private val fakeRepository = object : AirQualityRepository {
        override suspend fun fetchReadings() = readings
    }

    @Before
    fun setUp() {
        val viewModel = AirRunViewModel(fakeRepository, SettingsRepository())
        composeRule.setContent { AirRunTheme { AirRunApp(viewModel) } }
    }

    @Test
    fun settingsChanges_updateTheVerdictOnTheNowScreen() {
        composeRule.onNodeWithText("Go easy today").assertExists()

        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("Sensitive").performClick()
        composeRule.onNodeWithText("Now").performClick()

        composeRule.onNodeWithText("Take it indoors").assertExists()
        composeRule.onNodeWithText("RIGHT NOW · JOGGING · SENSITIVE").assertExists()
    }

    @Test
    fun changingRegion_switchesReadingsAndForecastTown() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("North").performClick()
        composeRule.onNodeWithText("North").assertIsSelected()
        composeRule.onNodeWithText("Mandai", substring = true).assertExists()

        composeRule.onNodeWithText("Now").performClick()

        composeRule.onNodeWithText("North Singapore").assertExists()
        composeRule.onNodeWithText("Good to run").assertExists()
    }

    @Test
    fun uvSwitch_greysOutTheUvTile() {
        composeRule.onNodeWithText("Settings").performClick()
        val uvSwitch = composeRule.onNodeWithText("UV index").performScrollTo()
        uvSwitch.assertIsOn()
        uvSwitch.performClick()
        uvSwitch.assertIsOff()

        composeRule.onNodeWithText("Now").performClick()

        composeRule.onNodeWithText("Not in verdict").assertExists()
    }
}
