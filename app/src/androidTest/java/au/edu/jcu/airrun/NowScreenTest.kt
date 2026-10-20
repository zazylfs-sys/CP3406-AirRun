package au.edu.jcu.airrun

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.jcu.airrun.ui.NowUiState
import au.edu.jcu.airrun.ui.PreviewData
import au.edu.jcu.airrun.ui.screens.NowScreen
import au.edu.jcu.airrun.ui.theme.AirRunTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NowScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun clearDay_showsGoodToRun_andAllFourReadings() {
        composeRule.setContent {
            AirRunTheme { NowScreen(NowUiState.Ready(PreviewData.clearDay), onRefresh = {}) }
        }

        composeRule.onNodeWithText("Good to run").assertIsDisplayed()
        composeRule.onNodeWithText("West Singapore").assertIsDisplayed()
        composeRule.onNodeWithText("UV index 6").assertIsDisplayed()
        composeRule.onNodeWithText("PSI · 24 hr").assertIsDisplayed()
        composeRule.onNodeWithText("Normal · Band I").assertIsDisplayed()
        composeRule.onNodeWithText("Partly Cloudy").assertIsDisplayed()
    }

    @Test
    fun hazeDay_sensitive_saysTakeItIndoors() {
        composeRule.setContent {
            AirRunTheme { NowScreen(NowUiState.Ready(PreviewData.hazeDaySensitive), onRefresh = {}) }
        }

        composeRule.onNodeWithText("Take it indoors").assertIsDisplayed()
        composeRule.onNodeWithText("PM2.5 168 µg/m³").assertIsDisplayed()
        composeRule.onNodeWithText("High · Band III").assertIsDisplayed()
    }

    @Test
    fun error_retryButtonCallsRefresh() {
        var refreshed = false
        composeRule.setContent {
            AirRunTheme { NowScreen(NowUiState.Error, onRefresh = { refreshed = true }) }
        }

        composeRule.onNodeWithText("Try again").performClick()

        assertTrue(refreshed)
    }
}
