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

sourceSets {
    main {
        java {
            srcDir("../app/src/main/java")
            exclude("**/MainActivity.java")
            exclude("**/*Activity.java")
        }
        resources {
            srcDirs("../app/src/main/assets")
        }
    }
}

application {
    mainClass.set("com.minimilitary3d.DesktopLauncher")
    applicationDefaultJvmArgs = listOf("-Xmx2G", "-Xms512M")
}

// === FAT-JAR: все зависимости + ассеты внутри ===
tasks.jar {
    archiveFileName.set("MiniMilitary3D.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.minimilitary3d.DesktopLauncher"
    }
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    }) {
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("META-INF/INDEX.LIST")
        exclude("module-info.class")
    }
}
