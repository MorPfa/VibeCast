import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "app.vibecast"
    compileSdk = 34

    defaultConfig {

        applicationId = "app.vibecast"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        val keystoreFile = project.rootProject.file("apikeys.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())
        val owmKey = properties.getProperty("OWM_KEY")
        buildConfigField(
            type = "String",
            name = "OWM_KEY",
            value = owmKey
        )
        val unsplashKey = properties.getProperty("UNSPLASH_KEY")
        buildConfigField(
            type = "String",
            name = "UNSPLASH_KEY",
            value = unsplashKey
        )
        val spotifyKey = properties.getProperty("SPOTIFY_KEY")
        buildConfigField(
            type = "String",
            name = "SPOTIFY_KEY",
            value = spotifyKey
        )
        val mapsKey = properties.getProperty("MAPS_KEY")
        buildConfigField(
            type = "String",
            name = "MAPS_KEY",
            value = mapsKey
        )

        addManifestPlaceholders(mapOf(
            "redirectSchemeName" to "spotify-sdk",
            "redirectHostName" to "auth"
        ))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {



    //Viewpager
    implementation("androidx.viewpager2:viewpager2:1.0.0")



//Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.firebaseui:firebase-ui-auth:7.2.0")

    
    //Datastore
//    implementation("androidx.datastore:datastore:1.0.0")
//    implementation ("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
//    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    // Location
    implementation("com.google.android.gms:play-services-location:21.0.1")

//    Spotify
    implementation ("com.spotify.android:auth:2.0.1")
    implementation("com.google.code.gson:gson:2.10")
    implementation("androidx.browser:browser:1.7.0")


//    Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.test:core-ktx:1.5.0")
    implementation(files("libs/spotify-app-remote-release-0.8.0.aar"))
    implementation("androidx.annotation:annotation:1.6.0")
    implementation("androidx.activity:activity:1.8.0")

    debugImplementation("androidx.fragment:fragment-testing:1.6.2")
    implementation("androidx.test.espresso:espresso-contrib:3.5.1")
    val room_version = "2.6.0"
    val lifecycle_version = "2.6.2"
    val work_version = "2.8.1"

//Places SDK
    implementation("com.google.android.libraries.places:places:3.3.0")


//    Work manager
    implementation("androidx.work:work-runtime-ktx:$work_version")
    androidTestImplementation("androidx.work:work-testing:$work_version")

//    Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")

//    Room
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    testImplementation("androidx.room:room-testing:$room_version")


//    Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

//    Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    implementation ("jp.wasabeef:glide-transformations:4.3.0")


//    Moshi
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation ("com.squareup.retrofit2:converter-moshi:2.9.0")

//    Viewmodel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
//    Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")


//    RecyclerView
    implementation ("androidx.recyclerview:recyclerview:1.3.1")

//Timber
    implementation("com.jakewharton.timber:timber:5.0.1")


    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
//    Mockito
    testImplementation ("org.mockito:mockito-core:5.5.0")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:4.1.0")
    //    Mockk
    testImplementation ("io.mockk:mockk:1.12.0")



    //Dagger Hilt Test
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")

    //Coroutine Test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")


    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Memory leak detector
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.8.1")

}
kapt {
    correctErrorTypes = true
}