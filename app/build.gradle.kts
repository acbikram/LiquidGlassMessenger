plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
 namespace = "com.example.liquidglass"
 compileSdk = 35
 defaultConfig { applicationId = "com.example.liquidglass"; minSdk = 29; targetSdk = 35; versionCode = 5; versionName = "0.5.0" }
 signingConfigs {
  create("release") {
   val storeFilePath = System.getenv("SIGNING_KEY_PATH")
   val storePasswordValue = System.getenv("SIGNING_KEY_PASSWORD")
   val keyAliasValue = System.getenv("SIGNING_KEY_ALIAS")
   val keyPasswordValue = System.getenv("SIGNING_PRIVATE_KEY_PASSWORD") ?: storePasswordValue
   if (!storeFilePath.isNullOrBlank() && !storePasswordValue.isNullOrBlank() && !keyAliasValue.isNullOrBlank() && !keyPasswordValue.isNullOrBlank()) {
    storeFile = file(storeFilePath)
    storePassword = storePasswordValue
    keyAlias = keyAliasValue
    keyPassword = keyPasswordValue
   }
  }
 }
 buildTypes { getByName("release") { isMinifyEnabled = false; signingConfig = signingConfigs.getByName("release") } }
 buildFeatures { compose = true }
 composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
dependencies {
 implementation("androidx.core:core-ktx:1.13.1")
 implementation("androidx.activity:activity-compose:1.9.1")
 implementation(platform("androidx.compose:compose-bom:2024.06.00"))
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-graphics")
 implementation("androidx.compose.foundation:foundation")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.material:material-icons-extended")
 implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
 implementation("androidx.security:security-crypto:1.1.0-alpha06")
 implementation("io.github.tdlib-android:core:0.1.1")
 debugImplementation("androidx.compose.ui:ui-tooling")
}