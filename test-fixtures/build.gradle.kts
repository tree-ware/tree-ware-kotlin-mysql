import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val hikariCpVersion = "5.0.1"
val testContainerVersion = "1.17.2"
val treeWareCoreVersion = "0.1.0.1"

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
    implementation("com.zaxxer:HikariCP:$hikariCpVersion")
    implementation("org.testcontainers:mysql:$testContainerVersion")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}