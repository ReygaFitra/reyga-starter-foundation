val projectGroup: String by project
val springBootVersion: String by project
val logbackClassicVersion: String by project
val janinoVersion: String by project

plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = projectGroup
            artifactId = "logging"
            version = project.version.toString()
        }
    }
}

dependencies {
    implementation(project(":common"))

    api("ch.qos.logback:logback-classic:$logbackClassicVersion")
    api("org.codehaus.janino:janino:$janinoVersion")
}

java {
    withJavadocJar()
    withSourcesJar()
}
