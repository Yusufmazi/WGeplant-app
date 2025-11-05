plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services") // Google Services Plugin
    id("jacoco")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.wgeplant"
    compileSdk = 35
    flavorDimensions += "version"

    defaultConfig {
        applicationId = "com.wgeplant"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunner = "com.wgeplant.CustomTestRunner"
    }

    productFlavors {
        create("normal") {
            dimension = "version"
        }
        create("uiTesting") {
            dimension = "version"
            testInstrumentationRunner = "com.wgeplant.HiltTestRunner"
            buildConfigField("String", "BASE_URL", "\"https://example.invalid/\"")
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

        getByName("debug") {
            isMinifyEnabled = false
            enableUnitTestCoverage = true // for local unit tests
            enableAndroidTestCoverage = true // for instrumentation tests
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = true
    config.setFrom(files("$projectDir/detekt-config.yml"))

    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }

    dependencies {
        detektPlugins(libs.detekt.formatting)
    }
}

ktlint {
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
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
    implementation(libs.firebase.auth)
    implementation(libs.google.material.views)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.dagger.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.junit.ktx)
    kapt(libs.dagger.hilt.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.mockito.android)

    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.androidx.hilt.compiler)
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    // converter
    implementation(libs.moshi.kotlin)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.kotlinx.serialization.json)

    // fcm
    implementation(libs.firebase.messaging)
    // firebase storage
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)
    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockito.core)
}

tasks.register("jacocoUnitTestReport", JacocoReport::class) {
    dependsOn(tasks.named("testNormalDebugUnitTest"))

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**///R.class",
        "**/R\$*.class",
        "**/BuildConfig.class",
        "**/Manifest.class",
        "**/Manifest\$*.class",
        "**/*_Hilt*.class",
        "**/*dagger*",
        "**/*Hilt*.class"
    )

    val sourceDirs = files(
        "src/main/java"
    )

    classDirectories.setFrom(
        fileTree("app/build/intermediates/javac/debug") {
            exclude(fileFilter)
        }
    )
    sourceDirectories.setFrom(sourceDirs)

    executionData.from(
        files("${layout.buildDirectory}/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
    )
}

tasks.register("jacocoInstrumentationTestReport", JacocoReport::class) {
    dependsOn(tasks.named("createUiTestingDebugCoverageReport"))

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**///R.class",
        "**/R\$*.class",
        "**/BuildConfig.class",
        "**/Manifest.class",
        "**/Manifest\$*.class",
        "**/*_Hilt*.class",
        "**/*dagger*",
        "**/*Hilt*.class"
    )

    val sourceDirs = files(
        "src/main/java"
    )

    classDirectories.setFrom(
        fileTree("app/build/intermediates/classes/debug") {
            exclude(fileFilter)
        }
    )
    sourceDirectories.setFrom(sourceDirs)

    executionData.from(
        fileTree(project.buildDir) {
            include("outputs/code-coverage/connected/*.ec")
        }
    )
}
