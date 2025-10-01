import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// The libraries are currently published to JitPack. JitPack picks up the
// version from the repo label, resulting in all libraries from the repo
// having the same version in JitPack. Setting the version for all projects
// conveys this.
allprojects {
    group = "org.tree-ware.tree-ware-kotlin-mysql"
    version = "0.6.0.0"
}

val log4j2Version = "2.19.0"
val mySqlConnectorVersion = "8.0.29"

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
    implementation(libs.treeWareKotlinCore)
    implementation(kotlin("stdlib"))

    implementation("mysql:mysql-connector-java:$mySqlConnectorVersion") {
        exclude("com.google.protobuf", "protobuf-java") // only needed for unused X DevAPI
    }

    testImplementation(project(":test-fixtures"))
    testImplementation(libs.treeWareKotlinCoreTestFixtures)
    testImplementation("org.apache.logging.log4j:log4j-core:${log4j2Version}")
    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:${log4j2Version}")
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