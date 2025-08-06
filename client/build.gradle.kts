import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
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
    commonMainImplementation(libs.kotlinx.coroutines)
    commonMainApi(libs.kotlinx.serialization.core)
    commonMainApi(libs.kotlinx.serialization.json)
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
