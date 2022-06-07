import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val mySqlConnectorVersion = "8.0.27"
val testContainerVersion = "1.17.2"
val mySqlEmbeddedVersion = "4.6.1"

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.6.10")
    id("idea")
    id("java-library")
    id("java-test-fixtures")
}

repositories {
    jcenter()
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    // Compile for Java 8 (default is Java 6)
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(project(":tree-ware-kotlin-core"))

    implementation(kotlin("stdlib"))

    implementation("mysql:mysql-connector-java:$mySqlConnectorVersion") {
        exclude("com.google.protobuf", "protobuf-java") // only needed for unused X DevAPI
    }

    testImplementation(project(":tree-ware-kotlin-core:test-fixtures"))
    testImplementation("org.testcontainers:mysql:$testContainerVersion")
    testImplementation(kotlin("test"))
    testImplementation("com.wix:wix-embedded-mysql:$mySqlEmbeddedVersion")
}

tasks.test {
    useJUnitPlatform()
}