import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-parcelize")
    id ("kotlin-kapt")


}

android {
    namespace = "com.example.theweatheroracle"
    compileSdk = 35

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }

    val apiKey = localProperties.getProperty("OPENWEATHER_API_KEY") ?: ""


    defaultConfig {
        applicationId = "com.example.theweatheroracle"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "OPENWEATHER_API_KEY", "\"$apiKey\"")

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE,LICENSE.md,LICENSE.txt,LICENSE-notice.md}"
        }
    }


}

val junitVersion = "4.13.2"
val hamcrestVersion = "2.2"
val archTestingVersion = "2.2.0"
val robolectricVersion = "4.13"
val androidXTestCoreVersion = "1.6.1"
val androidXTestExtKotlinRunnerVersion = "1.2.1"
val espressoVersion = "3.6.1"
val coroutinesVersion = "1.8.1"
val mockkVersion = "1.13.12"

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.play.services.location)
    implementation(libs.androidx.junit.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation (libs.androidx.room.runtime)
    kapt (libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx)

    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation("org.json:json:20231013")

    implementation(libs.kotlinx.coroutines.android)

    implementation (libs.androidx.core.ktx.v1120)

    implementation (libs.lottie)

    implementation (libs.osmdroid.android)

    implementation (libs.kotlinx.coroutines.play.services)

    implementation (libs.play.services.location)

    implementation (libs.androidx.work.runtime.ktx.v281)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // Application Dependencies
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Unit Test Dependencies
    testImplementation("junit:junit:$junitVersion")
    testImplementation("org.hamcrest:hamcrest:$hamcrestVersion")
    testImplementation("org.hamcrest:hamcrest-library:$hamcrestVersion")
    testImplementation("androidx.arch.core:core-testing:$archTestingVersion")
    testImplementation("org.robolectric:robolectric:$robolectricVersion")
    testImplementation("androidx.test:core-ktx:$androidXTestCoreVersion")
    testImplementation("androidx.test.ext:junit-ktx:$androidXTestExtKotlinRunnerVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.mockk:mockk-android:$mockkVersion")
    testImplementation("io.mockk:mockk-agent:$mockkVersion")

    // Instrumented Test Dependencies
    androidTestImplementation("androidx.test:core:$androidXTestCoreVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")
    androidTestImplementation("org.hamcrest:hamcrest:$hamcrestVersion")
    androidTestImplementation("org.hamcrest:hamcrest-library:$hamcrestVersion")
    androidTestImplementation("androidx.arch.core:core-testing:$archTestingVersion")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    androidTestImplementation("io.mockk:mockk-android:$mockkVersion")
    androidTestImplementation("io.mockk:mockk-agent:$mockkVersion")


}