package au.edu.jcu.airrun.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * False when the user has turned animations off (Developer options or
 * Accessibility > Remove animations). Decorative motion is then shown as a still frame.
 */
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    return remember(context) {
        inPreview || Settings.Global.getFloat(
            context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f,
        ) > 0f
    }
}
