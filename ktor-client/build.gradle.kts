import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.ktlint)
}

group = "me.y9san9.jsonrpc"

version = libs.versions.jsonrpc.get()

kotlin {
    explicitApi()

    compilerOptions {
        extraWarnings = true
        allWarningsAsErrors = true
        progressiveMode = true
    }

    jvm()
}

dependencies {
    commonMainApi(projects.client)
    commonMainImplementation(libs.kotlinx.coroutines)
    commonMainImplementation(libs.kotlinx.serialization.core)
    commonMainImplementation(libs.kotlinx.serialization.json)
    commonMainImplementation(libs.ktor.client.core)
    commonMainImplementation(libs.ktor.client.cio)
    commonMainImplementation(libs.ktor.client.websockets)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)

    pom {
        name = "jsonrpc"
        description = "Json RPC implementation in pure Kotlin"
        url = "https://github.com/y9san9/jsonrpc"

        licenses {
            license {
                name = "MIT"
                distribution = "repo"
                url = "https://github.com/y9san9/jsonrpc/blob/main/LICENSE.md"
            }
        }

        developers {
            developer {
                id = "y9san9"
                name = "Alex Sokol"
                email = "y9san9@gmail.com"
            }
        }

        scm {
            connection = "scm:git:ssh://github.com/y9san9/jsonrpc.git"
            developerConnection = "scm:git:ssh://github.com/y9san9/jsonrpc.git"
            url = "https://github.com/y9san9/jsonrpc"
        }
    }

    signAllPublications()
}
