rootProject.name = "mysql"

include("test-fixtures")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val treeWareKotlinCoreVersion = version("treeWareKotlinCoreVersion", "0.1.0.2")
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