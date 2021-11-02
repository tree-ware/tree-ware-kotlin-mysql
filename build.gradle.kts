import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val log4j2Version = "2.14.1"
val mySqlConnectorVersion = "8.0.27"
val mySqlEmbeddedVersion = "4.6.1"

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.5.21")
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
    implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")

    testImplementation(testFixtures(project(":tree-ware-kotlin-core")))
    testImplementation("com.wix:wix-embedded-mysql:$mySqlEmbeddedVersion")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
