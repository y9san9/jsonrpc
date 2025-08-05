enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "jsonrpc"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(
    "client",
    "ktor-client",
    "example",
)
