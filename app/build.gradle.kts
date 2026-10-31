import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
}

val keystorePropsFile = rootProject.file("release.properties")
val keystoreProps = Properties()

if (keystorePropsFile.exists()) {
    keystoreProps.load(FileInputStream(keystorePropsFile))
}

val hasValidSigningProps = keystorePropsFile.exists().also { exists ->
    if (exists) {
        FileInputStream(keystorePropsFile).use { keystoreProps.load(it) }
    }
}.let {
    listOf("storeFile", "storePassword", 
            "keyAlias", "keyPassword").all { key ->
        keystoreProps[key] != null
    }
}

// ⚡ ДЛЯ LIBGDX
configurations {
    create("natives")
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-stdlib:2.3.21")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.3.21")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.3.21")
        force("androidx.collection:collection:1.4.2")
        force("androidx.annotation:annotation:1.8.1")
        force("androidx.core:core-ktx:1.8.0")
        force("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
        force("androidx.collection:collection-ktx:1.4.2")
    }
}

android {
    namespace = "com.minimilitary3d"
    compileSdk = 36 
    
    lint {
        checkReleaseBuilds = false
    }
        
    signingConfigs {
        if (hasValidSigningProps) {
            create("release") {
                storeFile = rootProject.file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias = keystoreProps["keyAlias"] as String
                keyPassword = keystoreProps["keyPassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "com.minimilitary3d"
        minSdk = 21 
        targetSdk = 36  
        versionCode = 3
        versionName = "0.2-alfa"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 
        targetCompatibility = JavaVersion.VERSION_17 
    }

    buildTypes {
        release {
            if (hasValidSigningProps) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false // <--- ИСПРАВЛЕНО ТУТ
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }
    
    packaging {
        resources {
            resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            resources.excludes.add("META-INF/kotlinx_coroutines_core.version")

            // ⚡ ИСПРАВЛЯЕТ КОНФЛИКТ ШЕЙДЕРОВ (default.vs.glsl)
            resources.pickFirsts.add("net/mgsx/gltf/shaders/default.vs.glsl")
            resources.pickFirsts.add("net/mgsx/gltf/shaders/default.fs.glsl")
            resources.pickFirsts.add("net/mgsx/gltf/shaders/depth.vs.glsl")
            resources.pickFirsts.add("net/mgsx/gltf/shaders/depth.fs.glsl")

            resources.pickFirsts.add("nonJvmMain/default/linkdata/package_androidx/0_androidx.knm")
            resources.pickFirsts.add("nonJvmMain/default/linkdata/root_package/0_.knm")
            resources.pickFirsts.add("nonJvmMain/default/linkdata/module")

            resources.pickFirsts.add("nativeMain/default/linkdata/root_package/0_.knm")
            resources.pickFirsts.add("nativeMain/default/linkdata/module")

            resources.pickFirsts.add("commonMain/default/linkdata/root_package/0_.knm")
            resources.pickFirsts.add("commonMain/default/linkdata/module")
            resources.pickFirsts.add("commonMain/default/linkdata/package_androidx/0_androidx.knm")

            resources.pickFirsts.add("META-INF/kotlin-project-structure-metadata.json")

            resources.merges.add("commonMain/default/manifest")
            resources.merges.add("nonJvmMain/default/manifest")
            resources.merges.add("nativeMain/default/manifest")
        }
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.android.material)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.interpolator)
    
    // ⚡ LIBGDX
    val gdxVersion = "1.12.1"
    
    // Основной движок
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    
    // Нативные библиотеки (natives)
    add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-armeabi-v7a")
    add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-arm64-v8a")
    add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86")
    add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-x86_64")

    // ⚡ ШРИФТЫ (gdx-freetype) — ВОТ ОНИ, СТОЯТ ПОСЛЕ val gdxVersion
    implementation("com.badlogicgames.gdx:gdx-freetype:$gdxVersion")
    add("natives", "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-armeabi-v7a")
    add("natives", "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-arm64-v8a")
    add("natives", "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86")
    add("natives", "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-x86_64")

    // ⚡ GDX-GLTF (только core-модуль, без desktop и ibl-composer)
    implementation("com.github.mgsx-dev.gdx-gltf:gltf:2.3.0") {
        exclude(group = "com.github.mgsx-dev.gdx-gltf", module = "desktop")
        exclude(group = "com.github.mgsx-dev.gdx-gltf", module = "ibl-composer")
    }
}

tasks.register("copyAndroidNatives") {
    doFirst {
        file("src/main/jniLibs/").mkdirs()
        configurations.getByName("natives").files.forEach { jar ->
            var outputDir: java.io.File? = null
            if (jar.name.endsWith("natives-armeabi-v7a.jar")) outputDir = file("src/main/jniLibs/armeabi-v7a")
            if (jar.name.endsWith("natives-arm64-v8a.jar")) outputDir = file("src/main/jniLibs/arm64-v8a")
            if (jar.name.endsWith("natives-x86.jar")) outputDir = file("src/main/jniLibs/x86")
            if (jar.name.endsWith("natives-x86_64.jar")) outputDir = file("src/main/jniLibs/x86_64")
            if (outputDir != null) {
                copy {
                    from(zipTree(jar))
                    into(outputDir)
                    include("*.so")
                }
            }
        }
    }
}

tasks.whenTaskAdded {
    if (name.contains("merge") && name.contains("JniLibFolders")) {
        dependsOn("copyAndroidNatives")
    }
}