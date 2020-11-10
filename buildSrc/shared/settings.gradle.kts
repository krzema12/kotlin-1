buildscript {
    repositories {
        if (cacheRedirectorEnabled == 'true') {
            maven { url "https://cache-redirector.jetbrains.com/kotlin.bintray.com/kotlin-dependencies" }
        } else {
            maven { url "https://kotlin.bintray.com/kotlin-dependencies" }
        }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-build-gradle-plugin:0.0.20")
    }
}

def buildProperties = BuildPropertiesKt.getKotlinBuildPropertiesForSettings(settings)
def projectVersions = file("../../gradle/versions.properties").text


rootProject.name = "kotlin-native-shared"
