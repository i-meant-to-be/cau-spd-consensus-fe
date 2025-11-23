import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    // Default
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Hilt
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.dagger.hilt.android)

    // Global
    alias(libs.plugins.kotlin.serialization)

    // Proto Datastore
    alias(libs.plugins.google.protobuf)
}

android {
    namespace = "com.imeanttobe.consensusapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.imeanttobe.consensusapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Local properties is set here
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")

        // If local.properties exists, load it
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use {
                localProperties.load(it)
            }
        } else {
            println("local.properties file not found")
        }

        // Set the values from local.properties
        val isDevModeEnabled = localProperties.getProperty("config.isDevModeEnabled")?.toBoolean() ?: false

        // Set the buildConfigField
        buildConfigField("Boolean", "IS_DEV_MODE_ENABLED", isDevModeEnabled.toString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "4.1.2"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE.md,LICENSE-notice.md}"
        }

        jniLibs {
            excludes += "lib/**/libz.so"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.33.1"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin")
            }
        }
    }
}

dependencies {
    // Hilt
    ksp(libs.dagger.hilt.android.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.dagger.hilt.android)
    androidTestImplementation(libs.dagger.hilt.android.testing)

    // Global
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.material.icons.extended)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)

    // Proto Datastore
    implementation(libs.datastore.core)
    implementation(libs.datastore.preferences)
    implementation(libs.protobuf.kotlin.lite)

    // Default
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
