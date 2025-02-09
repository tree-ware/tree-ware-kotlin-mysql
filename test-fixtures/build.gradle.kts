import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val hikariCpVersion = "5.0.1"
val testContainerVersion = "1.17.2"

plugins {
    kotlin("jvm") version "1.7.0"
    id("idea")
    id("org.tree-ware.core") version "0.4.0.0"
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
    implementation(project(":"))
    implementation(libs.treeWareKotlinCore)
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