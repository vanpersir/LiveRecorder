plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.compose) // 添加Compose Compiler插件
}

android {
    namespace = "com.ds.liverecorder"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ds.liverecorder"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    // 使用版本目录引用
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)

    // Activity和Fragment
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.livedata.ktx)

    // Room数据库
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Gson
    implementation(libs.gson)
    
    // FlexboxLayout用于演出者标签布局
    implementation(libs.flexbox)

    // Jetpack Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.activity.compose)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    
    // 测试
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}