plugins { alias(libs.plugins.android.application) }
android {
    namespace = "com.itla.cajaenmanuel"
    compileSdk = 35
    defaultConfig { applicationId = "com.itla.cajaenmanuel"; minSdk = 24; targetSdk = 35; versionCode = 1; versionName = "1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
