pluginManagement {
    val rootProperties = java.util.Properties().apply {
        rootDir.resolve("../../kotlin-native/gradle.properties").reader().use(::load)
    }

    val kotlinCompilerRepo: String by rootProperties
    val kotlinVersion by rootProperties

    repositories {
        //maven(kotlinCompilerRepo)
        maven("https://cache-redirector.jetbrains.com/maven-central")
        mavenCentral()
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlin") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
		//useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${extra["bootstrapKotlinVersion"]}")
            }
        }
    }
}

rootProject.name = "kotlin-native-build-tools"

//includeBuild("../shared")
