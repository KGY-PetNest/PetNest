import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val mapkitApiKey: String = providers
    .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
    .asText
    .map { text ->
        Properties().apply { load(text.reader()) }
            .getProperty("MAPKIT_API_KEY")
            ?.takeIf { it.isNotBlank() }
    }
    .orElse("22129295-8bba-49a4-ae12-a3aef98532d1")
    .get()

android {
    namespace = "com.example.pet"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.pet"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "MAPKIT_API_KEY", "\"$mapkitApiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.yandex.mapkit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}