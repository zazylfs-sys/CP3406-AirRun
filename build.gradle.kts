// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

// Google Drive (mirror mode) drops hidden "Icon\r" files into every folder, including build
// output, and AAPT fails on them. When the project sits in Drive, build outside it instead.
// That also stops Drive from syncing hundreds of MB of intermediates.
if (rootDir.absolutePath.contains("My Drive") || rootDir.absolutePath.contains("GoogleDrive")) {
    allprojects {
        layout.buildDirectory.set(File(gradle.gradleUserHomeDir, "outside-drive-builds/${rootProject.name}/${project.name}"))
    }
}
