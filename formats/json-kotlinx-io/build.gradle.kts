/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
import Java9Modularity.configureJava9ModuleInfo
import org.jetbrains.dokka.gradle.*
import java.net.*

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

apply(from = rootProject.file("gradle/native-targets.gradle"))
apply(from = rootProject.file("gradle/configure-source-sets.gradle"))

kotlin {
    sourceSets {
        configureEach {
            languageSettings {
                optIn("kotlinx.serialization.internal.CoreFriendModuleApi")
                optIn("kotlinx.serialization.json.internal.JsonFriendModuleApi")
            }
        }
        val commonMain by getting {
            dependencies {
                api(project(":kotlinx-serialization-core"))
                api(project(":kotlinx-serialization-json"))
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.3.1-SNAPSHOT")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.3.1-SNAPSHOT")
            }
        }
    }
}

project.configureJava9ModuleInfo()
