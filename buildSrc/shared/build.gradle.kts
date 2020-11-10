/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("UnstableApiUsage")

//import org.jetbrains.kotlin.VersionGenerator
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    id("kotlin")
}

//val kotlinVersion: String by extra
//val buildKotlinVersion: String by extra
//val konanVersion: String by extra
//val kotlinCompilerRepo: String by extra
//val buildKotlinCompilerRepo: String by extra

group = "org.jetbrains.kotlin"
//version = konanVersion
buildscript {
    repositories {
        jcenter()
        maven("https://jetbrains.bintray.com/intellij-third-party-dependencies/")
        maven("https://kotlin.bintray.com/kotlin-dependencies")
        gradlePluginPortal()

        //maven("https://teamcity.jetbrains.com/guestAuth/app/rest/builds/buildType:(id:Kotlin_KotlinPublic_Compiler),number:1.4.30-dev-1423,branch:default:any,pinned:true/artifacts/content/maven")
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-build-gradle-plugin:0.0.20")
    }
}
repositories {
    jcenter()
    maven("https://jetbrains.bintray.com/intellij-third-party-dependencies/")
    maven("https://kotlin.bintray.com/kotlin-dependencies")
    gradlePluginPortal()

    //extra["bootstrapKotlinRepo"]?.let {
    //    maven(url = it)
    //}
    maven("https://teamcity.jetbrains.com/guestAuth/app/rest/builds/buildType:(id:Kotlin_KotlinPublic_Compiler),number:1.4.30-dev-1423,branch:default:any,pinned:true/artifacts/content/maven")
}

// FIXME(ddol): KLIB-REFACTORING-CLEANUP: drop generation of KonanVersion!
//val generateCompilerVersion by tasks.registering(VersionGenerator::class) {}

sourceSets["main"].withConvention(KotlinSourceSet::class) {
    kotlin.srcDir("src/main/kotlin")
    kotlin.srcDir("src/library/kotlin")
    //kotlin.srcDir(generateCompilerVersion.get().getVersionSourceDirectory())
}

tasks.withType<KotlinCompile> {
    //dependsOn(generateCompilerVersion)
    kotlinOptions.jvmTarget = "1.8"
}

tasks.clean {
    doFirst {
//        val versionSourceDirectory = generateCompilerVersion.get().getVersionSourceDirectory()
        //if (versionSourceDirectory.exists()) {
        //    versionSourceDirectory.delete()
        //}
    }
}

tasks.jar {
    archiveFileName.set("shared.jar")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${project.bootstrapKotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${project.bootstrapKotlinVersion}")
    api("org.jetbrains.kotlin:kotlin-native-utils:${project.bootstrapKotlinVersion}")
    api("org.jetbrains.kotlin:kotlin-util-klib:${project.bootstrapKotlinVersion}")

    kotlinCompilerClasspath("org.jetbrains.kotlin:kotlin-compiler-embeddable:${project.bootstrapKotlinVersion}")
}
