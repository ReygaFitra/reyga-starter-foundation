val projectGroup: String by project
val okHttpVersion: String by project

plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = projectGroup
            artifactId = "common-http"
            version = project.version.toString()

            pom {
                name.set("Reyga Starter Foundation HTTP")
                description.set("HTTP client contracts for Reyga Starter Foundation")
            }
        }
    }
}

dependencies {
    api("com.squareup.okhttp3:okhttp-jvm:$okHttpVersion")
}

java {
    withJavadocJar()
    withSourcesJar()
}
