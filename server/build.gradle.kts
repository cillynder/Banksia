plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    application
}

group = "moe.lava.banksia.server"
version = "1.0.0"
application {
    mainClass.set("moe.lava.banksia.server.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
}

dependencies {
    implementation(projects.core)
    implementation(projects.core.room)
    implementation(projects.server.gtfs)
    implementation(projects.server.gtfsRt)

    implementation(libs.logback)
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.kotlinx.serialization.csv)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.client.contentnegotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.contentnegotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.room.runtime)
    implementation(libs.sqlite.bundled)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}
