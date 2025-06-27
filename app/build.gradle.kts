import org.gradle.api.tasks.compile.JavaCompile
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.camilo.cocinarte"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.camilo.cocinarte"
        minSdk = 24
        targetSdk = 35
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.add("-Xlint:deprecation")
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity)
    implementation(libs.legacy.support.v4)

    // Architecture Components
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Retrofit y Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Firebase (usa BOM)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //authentication
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

//    cargar imagenes perfil
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // Flexbox
    implementation("com.google.android.flexbox:flexbox:3.0.0")

}