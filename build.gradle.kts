// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.ksp) apply false  // 添加KSP插件
}

// 添加阿里云镜像配置
buildscript {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // 保留官方源作为备用
        google()
        mavenCentral()
    }
}