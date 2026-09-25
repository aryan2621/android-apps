import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val googleWebClientId = localProperty("GOOGLE_WEB_CLIENT_ID")
val facebookAppId = localProperty("FACEBOOK_APP_ID")

android {
    namespace = "com.instashow"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.instashow"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Web client id lives in local.properties so it is not committed. A blank value keeps the app runnable.
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", googleWebClientId.get().toBuildConfigLiteral())
        // Optional Meta app id for Instagram story shares. Sharing works without it.
        buildConfigField("String", "FACEBOOK_APP_ID", facebookAppId.get().toBuildConfigLiteral())
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
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.health.connect)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.google.identity)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

fun Project.localProperty(name: String) = providers.of(LocalPropertyValueSource::class.java) {
    parameters.propertiesFile.set(rootProject.layout.projectDirectory.file("local.properties"))
    parameters.propertyName.set(name)
}

fun String.toBuildConfigLiteral(): String {
    val escaped = replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "").replace("\r", "")
    return "\"$escaped\""
}

abstract class LocalPropertyValueSource : ValueSource<String, LocalPropertyValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val propertiesFile: RegularFileProperty
        val propertyName: Property<String>
    }

    override fun obtain(): String {
        val file = parameters.propertiesFile.orNull?.asFile ?: return ""
        if (!file.exists()) return ""
        val properties = Properties()
        file.inputStream().use { properties.load(it) }
        return properties.getProperty(parameters.propertyName.get()).orEmpty()
    }
}