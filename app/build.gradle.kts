plugins {
    id("com.android.application") version "8.9.1"
    id("org.jetbrains.kotlin.android") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

android {
    namespace = "com.example.kodydomofonowe"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kodydomofonowe"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}


dependencies {
    implementation("androidx.compose.ui:ui:1.4.0") // Zaktualizowana wersja
    implementation("androidx.compose.material3:material3:1.1.0") // Kompatybilna z wersją UI
    implementation("androidx.activity:activity-compose:1.7.0") // Zaktualizowana wersja
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.0")
    implementation("androidx.compose.ui:ui-tooling:1.4.0")
    implementation("androidx.compose.ui:ui-graphics:1.4.0")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("androidx.compose.material:material:1.4.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.compose.material3:material3:1.2.1")

    implementation("androidx.compose.material:material-icons-extended")







}





repositories {
    google()
    mavenCentral()
}

