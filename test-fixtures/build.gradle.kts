import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val hikariCpVersion = "5.0.1"
val testContainerVersion = "1.17.2"

plugins {
    kotlin("jvm") version "2.1.10"
    id("idea")
    id("org.tree-ware.core") version "0.5.2.0"
    id("java-library")
    id("maven-publish")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
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