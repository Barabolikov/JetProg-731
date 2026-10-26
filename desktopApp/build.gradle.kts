import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(compose.material) // Базовий Material Design
    implementation(compose.materialIconsExtended) // Розширені іконки (включаючи Delete)
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "com.example.jetprog.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.example.jetprog"
            packageVersion = "1.0.0"
        }
    }
}