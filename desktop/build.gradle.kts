plugins {
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val gdxVersion = "1.12.1"

dependencies {
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    runtimeOnly("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx-freetype:$gdxVersion")
    runtimeOnly("com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop")
    implementation("com.github.mgsx-dev.gdx-gltf:gltf:2.3.0")
}

application {
    mainClass.set("com.minimilitary3d.DesktopLauncher")
    applicationDefaultJvmArgs = listOf("-Xmx2G", "-Xms512M")
}

val assetsDir = file("../app/src/main/assets")

tasks.named<JavaExec>("run") {
    workingDir = assetsDir
    classpath = sourceSets.main.get().runtimeClasspath
}
