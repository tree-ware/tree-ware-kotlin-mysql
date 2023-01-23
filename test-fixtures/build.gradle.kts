import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val hikariCpVersion = "5.0.1"
val testContainerVersion = "1.17.2"
val treeWareCoreVersion = "0.1.0.1"

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.7.0")
    id("idea")
    id("java-library")
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
