rootProject.name = "mysql"

include("test-fixtures")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.tree-ware.core") {
                useModule("org.tree-ware.tree-ware-gradle-core-plugin:core-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val treeWareKotlinCoreVersion = version("treeWareKotlinCoreVersion", "0.4.0.0")
            library("treeWareKotlinCore", "org.tree-ware.tree-ware-kotlin-core", "core").versionRef(
                treeWareKotlinCoreVersion
            )
            library(
                "treeWareKotlinCoreTestFixtures",
                "org.tree-ware.tree-ware-kotlin-core",
                "test-fixtures"
            ).versionRef(treeWareKotlinCoreVersion)
        }
    }
}