val projectGroup: String by project
val springBootVersion: String by project
val logbackClassicVersion: String by project

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

    api("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    api("ch.qos.logback:logback-classic:$logbackClassicVersion")
}

java {
    withJavadocJar()
    withSourcesJar()
}
