plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    compile(project(":core:descriptors"))
    compileOnly(intellijCoreDep()) { includeJars("intellij-core") }
    compileOnly(intellijDep()) { includeJars("trove4j") }

    testCompile(commonDep("junit:junit"))
}

sourceSets {
    "main" {
        projectDefault()
        resources.srcDir("src/resources")
    }
    "test" {
        projectDefault()
        resources.srcDir("tests/resources")
    }
}
