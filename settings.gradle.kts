pluginManagement {
  repositories {
    google {
      content {
        // Do not try to resolve Maven Central-only POMs (including parents) from Google Maven.
        includeGroupByRegex("com\\.android(\\..*)?")
        includeGroupByRegex("androidx(\\..*)?")
        includeGroupByRegex("android\\.arch(\\..*)?")
        includeGroupByRegex("com\\.google(\\..*)?")
      }
    }
    mavenCentral()
    gradlePluginPortal {
      content { excludeGroupByRegex("org\\.apache(\\..*)?") }
    }
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google {
      content {
        // Do not try to resolve Maven Central-only POMs (including parents) from Google Maven.
        includeGroupByRegex("com\\.android(\\..*)?")
        includeGroupByRegex("androidx(\\..*)?")
        includeGroupByRegex("android\\.arch(\\..*)?")
        includeGroupByRegex("com\\.google(\\..*)?")
      }
    }
    mavenCentral()
  }
}

rootProject.name = "Light"

include(":app")
