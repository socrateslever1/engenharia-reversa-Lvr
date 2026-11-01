plugins { id("com.android.application") }

android {
    namespace = "br.com.lever.unitvhybrid"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.lever.unitvhybrid.prototype"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-alpha"
    }
    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation(project(":integration"))
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")
    testImplementation("junit:junit:4.13.2")
}
