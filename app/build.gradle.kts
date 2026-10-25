plugins {
    id("com.android.application")
}

val releaseStoreFile = providers.gradleProperty("GOALCHALLENGE_RELEASE_STORE_FILE").orNull
val releaseStorePassword = providers.gradleProperty("GOALCHALLENGE_RELEASE_STORE_PASSWORD").orNull
val releaseKeyAlias = providers.gradleProperty("GOALCHALLENGE_RELEASE_KEY_ALIAS").orNull
val releaseKeyPassword = providers.gradleProperty("GOALCHALLENGE_RELEASE_KEY_PASSWORD").orNull
val releaseSigningValues = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
)
val releaseSigningConfigured = releaseSigningValues.all { !it.isNullOrBlank() }
val releaseSigningPartiallyConfigured =
    releaseSigningValues.any { !it.isNullOrBlank() } && !releaseSigningConfigured

check(!releaseSigningPartiallyConfigured) {
    "Configure all four GOALCHALLENGE_RELEASE_* Gradle properties to sign a release."
}

android {
    namespace = "com.goalchallenge.game"
    compileSdk = 36

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = rootProject.file(requireNotNull(releaseStoreFile))
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.goalchallenge.game"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":game-core"))
}

tasks.configureEach {
    if (name == "packageReleaseBundle") {
        doFirst {
            check(releaseSigningConfigured) {
                "Release bundle is not signed. Configure all four GOALCHALLENGE_RELEASE_* Gradle properties before building for Google Play."
            }
        }
    }
}
