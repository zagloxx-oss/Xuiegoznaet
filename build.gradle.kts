buildscript {
    // Репозитории убраны, так как они берутся из settings.gradle.kts
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}