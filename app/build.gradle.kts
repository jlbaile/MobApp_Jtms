plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.jtms30032026"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.jtms30032026"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packagingOptions {
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE")
        exclude("META-INF/NOTICE")
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/LICENSE.txt")
        exclude("META-INF/ASL2.0")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation("com.android.volley:volley:1.2.1")

    // poi only — no poi-ooxml, avoids xmlbeans duplicate class conflict
    implementation("org.apache.poi:poi:3.17") {
        exclude(group = "org.apache.xmlbeans", module = "xmlbeans")
    }

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}