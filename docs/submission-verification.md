# Submission verification

Checked on 20 October 2026 for CP3406 Assessment 1.

| Check | Result |
|---|---|
| Debug APK build | `:app:assembleDebug` succeeded |
| JVM unit tests | 39 tests passed; 0 failures, errors or skips. The test task was explicitly rerun. |
| Android Lint | 0 errors; 16 warnings, all for newer dependency or plugin versions |
| Live NEA feeds | PSI, PM2.5, UV and two-hour forecast each returned HTTP 200, API code 0 and data without an API key |
| Compose UI test rerun | Not completed: the API 36.1 emulator failed to finish booting/respond to device-property queries, and the runner reported no compatible device. This check does not establish a new UI pass. |

Commands used:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain
./gradlew :app:testDebugUnitTest --rerun :app:connectedDebugAndroidTest --console=plain
```

The second command successfully reran the JVM tests, then failed at the connected-device test task. Six Compose UI tests are included in `app/src/androidTest`; run them again on a fully booted emulator or a physical Android device.

The live-feed checks used the same `okhttp/4.12.0` user-agent string as the app. They verify endpoint availability from this machine, not an end-to-end run of the Android UI. Unit-test API parsing and repository behaviour use the included response fixtures and fakes.

Machine-specific files, build outputs, personal reflection documents and any local credentials are excluded from the public repository.
