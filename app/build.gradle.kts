plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.planteye"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.planteye"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
    }
    dependencies {
        // ...
        implementation ("androidx.camera:camera-camera2:1.0.0-rc01")
        implementation ("androidx.camera:camera-lifecycle:1.0.0-rc01")
        implementation ("androidx.camera:camera-view:1.0.0-alpha26")
        implementation ("androidx.camera:camera-core:1.x.x")
        implementation ("androidx.camera:camera-camera2:1.x.x")
        implementation ("androidx.camera:camera-lifecycle:1.x.x")
        implementation ("androidx.camera:camera-view:1.x.x")
        implementation("com.microsoft.device.display:display-mask:0.3.0")
        implementation ("com.microsoft.graph:microsoft-graph:5.71.x")  // replace x.x with the latest version
        implementation ("com.microsoft.identity.client:msal:4.7.0")  // replace x.x with the latest version


    }
    repositories {
        // maven {
        //     url 'https://example.com/maven-repo'
        // }
    }



}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.5.3")
    implementation("androidx.navigation:navigation-ui:2.5.3")
    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation ("androidx.camera:camera-camera2:1.0.0-rc01")
    implementation("com.microsoft.device.display:display-mask:0.3.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("com.microsoft.graph:microsoft-graph:5.71.0")
    implementation ("com.microsoft.identity.client:msal:4.7.0")
    implementation ("com.microsoft.identity.client:msal:4.7.0")
    implementation("com.microsoft.identity.client:msal:4.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}


