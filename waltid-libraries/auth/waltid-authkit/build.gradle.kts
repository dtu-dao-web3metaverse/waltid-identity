plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin") version "2.3.12"
    id("maven-publish")

    application

    id("com.github.ben-manes.versions")
}

group = "id.walt"
version = "0.0.1"

application {
    mainClass.set("id.walt.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.waltid.dev/releases")
}

dependencies {
    // Auth methods

    // RADIUS
    implementation("org.aaa4j.radius:aaa4j-radius-client:0.3.0")

    // LDAP
    //implementation("org.apache.directory.server:apacheds-server-integ:2.0.0.AM27")
    implementation("org.apache.directory.api:api-all:2.1.6")

    // TOTP/HOTP
    implementation("com.atlassian:onetime:2.1.1")

    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    //implementation("io.ktor:ktor-server-auth-ldap-jvm")
    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-apache-jvm")
    implementation("io.ktor:ktor-server-sessions-jvm")
    implementation("io.ktor:ktor-server-auto-head-response-jvm")
    implementation("io.ktor:ktor-server-double-receive-jvm")
    implementation("io.ktor:ktor-server-webjars-jvm")
    implementation("io.ktor:ktor-server-host-common-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-default-headers-jvm")
    implementation("io.ktor:ktor-server-forwarded-header-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-cio-jvm")

    // Ktor client
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")

    // Ktor shared
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    // Ktor server external
    implementation("io.github.smiley4:ktor-swagger-ui:3.2.0")

    // Logging
    implementation("io.klogging:klogging-jvm:0.7.0")
    implementation("io.klogging:slf4j-klogging:0.7.0")

    /* --- Testing --- */

    // Ktor
    testImplementation("io.ktor:ktor-server-cio-jvm")
    testImplementation("io.ktor:ktor-server-tests-jvm")

    // Kotlin
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            pom {
                name.set("walt.id AuthKit")
                description.set(
                    """
                    Kotlin/Java library for AuthNZ
                    """.trimIndent()
                )
                url.set("https://walt.id")
            }
            from(components["java"])
        }
    }

    repositories {
        maven {
            val releasesRepoUrl = uri("https://maven.waltid.dev/releases")
            val snapshotsRepoUrl = uri("https://maven.waltid.dev/snapshots")
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            val envUsername = System.getenv("MAVEN_USERNAME")
            val envPassword = System.getenv("MAVEN_PASSWORD")

            val usernameFile = File("secret_maven_username.txt")
            val passwordFile = File("secret_maven_password.txt")

            val secretMavenUsername = envUsername ?: usernameFile.let { if (it.isFile) it.readLines().first() else "" }
            val secretMavenPassword = envPassword ?: passwordFile.let { if (it.isFile) it.readLines().first() else "" }

            credentials {
                username = secretMavenUsername
                password = secretMavenPassword
            }
        }
    }
}