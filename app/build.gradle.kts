plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.google.imageassets.xc6331b7a83a5b027274d88c6a9bb9551"
    compileSdk = 34

    signingConfigs {
        create("release") {
            keyAlias = "key0"
            keyPassword = "4815912"
            storeFile = file("sign.jks")
            storePassword = "4815912"
        }
    }
    defaultConfig {
        applicationId = "com.google.imageassets.xc6331b7a83a5b027274d88c6a9bb9551"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
