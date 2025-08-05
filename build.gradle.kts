plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.maven.publish) apply false
}

tasks {
    val printVersion by registering {
        group = "CI"

        doFirst {
            println(libs.versions.jsonrpc.get())
        }
    }
}
