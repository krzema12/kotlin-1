import java.util.Properties

extra["versions.native-platform"] = "0.14"

buildscript {

    val cacheRedirectorEnabled = findProperty("cacheRedirectorEnabled")?.toString()?.toBoolean() == true

    extra["defaultSnapshotVersion"] = kotlinBuildProperties.defaultSnapshotVersion
    kotlinBootstrapFrom(BootstrapOption.SpaceBootstrap(kotlinBuildProperties.kotlinBootstrapVersion!!, cacheRedirectorEnabled))

    repositories {
        if (cacheRedirectorEnabled) {
            maven("https://cache-redirector.jetbrains.com/jcenter.bintray.com")
            maven("https://cache-redirector.jetbrains.com/kotlin.bintray.com/kotlin-dependencies")
        } else {
            jcenter()
            maven("https://kotlin.bintray.com/kotlin-dependencies")
        }

        project.bootstrapKotlinRepo?.let {
            maven(url = it)
        }
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-build-gradle-plugin:0.0.20")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.bootstrapKotlinVersion}")
        classpath("org.jetbrains.kotlin:kotlin-sam-with-receiver:${project.bootstrapKotlinVersion}")
    }
}

val cacheRedirectorEnabled = findProperty("cacheRedirectorEnabled")?.toString()?.toBoolean() == true

logger.info("buildSrcKotlinVersion: " + extra["bootstrapKotlinVersion"])
logger.info("buildSrc kotlin compiler version: " + org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION)
logger.info("buildSrc stdlib version: " + KotlinVersion.CURRENT)

apply {
    plugin("kotlin")
    plugin("kotlin-sam-with-receiver")
    plugin("groovy")

    from("../gradle/checkCacheability.gradle.kts")
}

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        register("jps-compatible") {
            id = "jps-compatible"
            implementationClass = "org.jetbrains.kotlin.pill.JpsCompatiblePlugin"
        }
    }
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

fun Project.getBooleanProperty(name: String): Boolean? = this.findProperty(name)?.let {
    val v = it.toString()
    if (v.isBlank()) true
    else v.toBoolean()
}

rootProject.apply {
    from(rootProject.file("../gradle/versions.gradle.kts"))
}
val rootProperties = Properties().apply {
    rootDir.resolve("../kotlin-native/gradle.properties").reader().use(::load)
}
val slackApiVersion = rootProperties["slackApiVersion"]
val ktorVersion = rootProperties["ktorVersion"]
val shadowVersion = rootProperties["shadowVersion"]
val metadataVersion = rootProperties["metadataVersion"]

val isTeamcityBuild = kotlinBuildProperties.isTeamcityBuild
val intellijUltimateEnabled by extra(kotlinBuildProperties.intellijUltimateEnabled)
val intellijSeparateSdks by extra(project.getBooleanProperty("intellijSeparateSdks") ?: false)

extra["intellijReleaseType"] = when {
    extra["versions.intellijSdk"]?.toString()?.contains("-EAP-") == true -> "snapshots"
    extra["versions.intellijSdk"]?.toString()?.endsWith("SNAPSHOT") == true -> "nightly"
    else -> "releases"
}

extra["versions.androidDxSources"] = "5.0.0_r2"

extra["customDepsOrg"] = "kotlin.build"

repositories {
    jcenter()
    maven("https://jetbrains.bintray.com/intellij-third-party-dependencies/")
    maven("https://kotlin.bintray.com/kotlin-dependencies")
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://dl.bintray.com/kotlin/kotlin-dev")
    gradlePluginPortal()

    extra["bootstrapKotlinRepo"]?.let {
        maven(url = it)
    }
}

dependencies {
    implementation(kotlin("stdlib", embeddedKotlinVersion))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.bootstrapKotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-build-gradle-plugin:0.0.20")
    implementation("com.gradle.publish:plugin-publish-plugin:0.12.0")

    implementation("net.rubygrapefruit:native-platform:${property("versions.native-platform")}")
    implementation("net.rubygrapefruit:native-platform-windows-amd64:${property("versions.native-platform")}")
    implementation("net.rubygrapefruit:native-platform-windows-i386:${property("versions.native-platform")}")
    implementation("com.jakewharton.dex:dex-method-list:3.0.0")

    implementation("com.github.jengelman.gradle.plugins:shadow:${rootProject.extra["versions.shadow"]}")
    implementation("net.sf.proguard:proguard-gradle:6.2.2")
    implementation("org.jetbrains.intellij.deps:asm-all:8.0.1")

    implementation("gradle.plugin.org.jetbrains.gradle.plugin.idea-ext:gradle-idea-ext:0.5")

    implementation("org.gradle:test-retry-gradle-plugin:1.1.9")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${project.bootstrapKotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${project.bootstrapKotlinVersion}")
    //api("org.jetbrains.kotlin:kotlin-native-utils:${project.bootstrapKotlinVersion}")
    api("org.jetbrains.kotlin:kotlin-util-klib:${project.bootstrapKotlinVersion}")

    compileOnly(gradleApi())

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.bootstrapKotlinVersion}")
    implementation("com.ullink.slack:simpleslackapi:$slackApiVersion")

    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    api("org.jetbrains.kotlin:kotlin-native-utils:${project.bootstrapKotlinVersion}")
    implementation("com.github.jengelman.gradle.plugins:shadow:$shadowVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-klib:$metadataVersion")
    implementation ("junit:junit:4.12")
    implementation ("org.jetbrains.kotlin:kotlin-test:${project.bootstrapKotlinVersion}")
    implementation ("org.jetbrains.kotlin:kotlin-test-junit:${project.bootstrapKotlinVersion}")
}

samWithReceiver {
    annotation("org.gradle.api.HasImplicitReceiver")
}

fun Project.`samWithReceiver`(configure: org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverExtension.() -> Unit): Unit =
        extensions.configure("samWithReceiver", configure)

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
tasks.validatePlugins.configure {
    enabled = false
}
tasks["build"].dependsOn(":prepare-deps:build")

sourceSets["main"].withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
    kotlin.srcDir("src/main/kotlin")
    kotlin.srcDir("shared/src/library/kotlin")
    kotlin.srcDir("shared/src/main/kotlin")
    kotlin.srcDir("build-tools/src/main/kotlin")
    kotlin.srcDir("../kotlin-native/tools/kotlin-native-gradle-plugin/src/main/kotlin")
    //kotlin.srcDir(generateCompilerVersion.get().getVersionSourceDirectory())
}


sourceSets["main"].withConvention(org.gradle.api.tasks.GroovySourceSet::class) {
    groovy.srcDir("build-tools/src/main/groovy")
}

//tasks.named("compileGroovy", GroovyCompile::class.java) {
//    classpath += project.files()
//    //classpath = sourceSets["main"].compileClasspath
//}

tasks.named("compileGroovy", GroovyCompile::class.java) {
    classpath += project.files(tasks.named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java))
    dependsOn(tasks.named("compileKotlin"))
}

allprojects {
    tasks.register("checkBuild")

    afterEvaluate {
        apply(from = "$rootDir/../gradle/cacheRedirector.gradle.kts")
    }
}
