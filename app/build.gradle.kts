import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.vuongnk.appandroid"
    compileSdk = 34

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    val CLOUD_NAME: String = localProperties.getProperty("CLOUD_NAME", "")
    val API_KEY: String = localProperties.getProperty("API_KEY", "")
    val API_SECRET: String = localProperties.getProperty("API_SECRET", "")
    val BASE_URL_SEND_MESSAGE: String = localProperties.getProperty("BASE_URL_SEND_MESSAGE", "")

    defaultConfig {
        applicationId = "com.vuongnk.appandroid"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CLOUD_NAME", "\"$CLOUD_NAME\"")
        buildConfigField("String", "API_KEY", "\"$API_KEY\"")
        buildConfigField("String", "API_SECRET", "\"$API_SECRET\"")
        buildConfigField("String", "BASE_URL_SEND_MESSAGE", "\"$BASE_URL_SEND_MESSAGE\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        buildConfig = true
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"  // Thêm dòng này
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")

}