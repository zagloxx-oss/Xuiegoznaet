pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") } // ⚡ ДОБАВЛЕНО ДЛЯ GDX-GLTF
  }
}

rootProject.name = "Mini Military 3D "


include(":app")
include(":desktop")
