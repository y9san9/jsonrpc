plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
}

kotlin {
    jvm()
}

dependencies {
    commonMainImplementation(projects.ktorClient)
    commonMainImplementation(libs.ktor.client.core)
}
