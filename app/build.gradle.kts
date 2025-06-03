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

        // Añadir para compatibilidad con nuevas APIs de almacenamiento
        vectorDrawables.useSupportLibrary = true
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
        // Habilitar funciones de Java 8
        isCoreLibraryDesugaringEnabled = true
    }


    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.legacy.support.v4)

    // Flexbox
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // Gson
    implementation("com.google.code.gson:gson:2.11.0")

    // Glide (para mostrar imágenes)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Para nuevas APIs de almacenamiento (necesario para Android 10+)
    implementation("androidx.documentfile:documentfile:1.1.0")

    // ActivityResult API (mejor manejo de permisos e intents)
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Desugaring para APIs modernas en versiones antiguas
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}