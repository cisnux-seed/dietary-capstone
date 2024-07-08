@file:Suppress("UnstableApiUsage")

import com.google.protobuf.gradle.GenerateProtoTask
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool

plugins {
    kotlin("plugin.serialization")
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.google.protobuf)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.com.google.devtools.ksp)
}

android {
    namespace = "org.cisnux.mydietary"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.cisnux.mydietary"
        minSdk = 26
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "org.cisnux.mydietary.CustomTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions {
        animationsDisabled = true
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
    kotlin {
        sourceSets.all {
            languageSettings.enableLanguageFeature("ExplicitBackingFields")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.26.1"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            val protoTask =
                project.tasks.getByName("generate" + variant.name.replaceFirstChar { it.uppercaseChar() } + "Proto") as GenerateProtoTask

            project.tasks.getByName("ksp" + variant.name.replaceFirstChar { it.uppercaseChar() } + "Kotlin") {
                dependsOn(protoTask)
                (this as AbstractKotlinCompileTool<*>).setSource(
                    protoTask.outputBaseDir
                )
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // constraintlayout compose
    implementation(libs.androidx.constraintlayout.compose)
    // hilt
    implementation(libs.google.dagger.hilt.android)
    androidTestImplementation(libs.google.dagger.hilt.android.testing)
    ksp(libs.google.dagger.hilt.android.compiler)
    // navigation compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.testing)
    // hilt navigation compose
    implementation(libs.androidx.hilt.navigation.compose)
    // camerax
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.view)
    // lottie
    implementation(libs.airbnb.lottie.compose)
    // coil
    implementation(libs.coil.compose)
    // ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    debugImplementation(libs.slf4j.simple)
    // monad
    implementation(libs.arrow.core)
    // shimmer
    implementation(libs.compose.shimmer)
    // testing
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    // datastore
    implementation(libs.datastore.proto)
    implementation(libs.datastore.preferences)
    implementation(libs.protobuf.javalite)
    // vico charts
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    //widget
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.appwidget.preview)
    implementation(libs.androidx.glance.preview)
    //firebase
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    //UI automator
    androidTestImplementation(libs.androidx.uiautomator)
}