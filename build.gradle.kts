import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// The libraries are currently published to JitPack. JitPack picks up the
// version from the repo label, resulting in all libraries from the repo
// having the same version in JitPack. Setting the version for all projects
// conveys this.
allprojects {
    group = "org.tree-ware.tree-ware-kotlin-mysql"
    version = "0.1.0.2"
}

val mySqlConnectorVersion = "8.0.29"
val okioVersion = "3.2.0"
val treeWareCoreVersion = "0.1.0.2"

plugins {
    kotlin("jvm") version "1.7.0"
    id("idea")
    id("java-library")
    id("maven-publish")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

tasks.withType<KotlinCompile> {
    // Compile for Java 8 (default is Java 6)
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation("org.tree-ware.tree-ware-kotlin-core:core:$treeWareCoreVersion")
    implementation(kotlin("stdlib"))

    implementation("mysql:mysql-connector-java:$mySqlConnectorVersion") {
        exclude("com.google.protobuf", "protobuf-java") // only needed for unused X DevAPI
    }

    testImplementation(project(":test-fixtures"))
    testImplementation("org.tree-ware.tree-ware-kotlin-core:test-fixtures:$treeWareCoreVersion")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform {
        when (System.getProperty("integrationTests", "")) {
            "include" -> includeTags("integrationTest")
            "exclude" -> excludeTags("integrationTest")
            else -> {}
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}