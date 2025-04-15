// 추가됨
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.15")
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Root-level build.gradle.kts

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false // ✅ 꼭 필요!
}
