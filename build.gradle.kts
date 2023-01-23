import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.tree-ware"
version = "1.0-SNAPSHOT"

val mySqlConnectorVersion = "8.0.29"
val okioVersion = "3.2.0"
val treeWareCoreVersion = "0.1.0.1"
val treeWareCoreTestFixturesVersion = "0.1.0.0"

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
    implementation(kotlin("stdlib"))

    implementation("mysql:mysql-connector-java:$mySqlConnectorVersion") {
        exclude("com.google.protobuf", "protobuf-java") // only needed for unused X DevAPI
    }

    testImplementation(project(":test-fixtures"))
    testImplementation("org.tree-ware.tree-ware-kotlin-core:test-fixtures:$treeWareCoreTestFixturesVersion")
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