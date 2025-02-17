/*
 * Copyright 2021-2024 VastGui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URL

plugins {
    kotlin("android")
    id("com.android.library")
    id("convention.publication")
    id("org.jetbrains.dokka")
}

android {
    namespace = "com.ave.vastgui.mars"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        ndk { abiFilters.addAll(arrayOf("armeabi-v7a", "arm64-v8a")) }
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
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    sourceSets["main"].jniLibs.srcDirs("libs")

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("com.log.vastgui.core.annotation.LogApi")
}

dependencies {
    compileOnly(projects.libraries.kernel)
    compileOnly(projects.libraries.log.core)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
}

extra["PUBLISH_ARTIFACT_ID"] = "log-mars"
extra["PUBLISH_DESCRIPTION"] = "Tencent xlog for log"
extra["PUBLISH_URL"] =
    "https://github.com/SakurajimaMaii/Android-Vast-Extension/tree/develop/libraries/log/mars"

val mavenPropertiesFile = File(rootDir, "maven.properties")
if (mavenPropertiesFile.exists()) {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "io.github.sakurajimamaii"
                artifactId = "log-mars"
                version = "1.3.10"

                afterEvaluate {
                    from(components["release"])
                }
            }
        }
    }
}

tasks.withType<DokkaTaskPartial> {
    moduleName.set("log-mars")
    dokkaSourceSets.configureEach {
        sourceLink {
            // FIXME https://github.com/Kotlin/dokka/issues/2876
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl.set(URL("https://github.com/SakurajimaMaii/Android-Vast-Extension/tree/develop/libraries/log/mars/src"))
            remoteLineSuffix.set("#L")
        }
    }
}