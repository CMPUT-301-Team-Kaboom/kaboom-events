plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.projecteventlotteryapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.projecteventlotteryapp"
        minSdk = 26
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions{
        unitTests.isReturnDefaultValues = true;
    }
}

dependencies {
    //      Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage")
    
    // Use compileOnly for the SDK platform jars to avoid dexing errors while allowing Javadoc generation
    compileOnly(fileTree(mapOf<String, Any>(
        "dir" to "/Users/kevincao/Library/Android/sdk/platforms/android-36",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to emptyList<String>()
    )))

    // JUnit5 unit testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.0.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.0.1")
    testImplementation("com.google.android.gms:play-services-tasks:18.0.2")

    //    Glide dependency (image loading)
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    //    Mockito dependencies
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("org.mockito:mockito-android:5.5.0")

    //    default dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
