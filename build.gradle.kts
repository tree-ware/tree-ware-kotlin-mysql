import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val hikariCpVersion = "5.0.1"
val mySqlConnectorVersion = "8.0.29"
val testContainerVersion = "1.17.2"

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.7.0")
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
    testImplementation(kotlin("test"))

    testFixturesImplementation("com.zaxxer:HikariCP:$hikariCpVersion")
    testFixturesImplementation("org.testcontainers:mysql:$testContainerVersion")
}

tasks.test {
    useJUnitPlatform()
}