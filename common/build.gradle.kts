val projectGroup: String by project
val springBootVersion: String by project

plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = projectGroup
            artifactId = "common"
            version = project.version.toString()
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
}

java {
    withJavadocJar()
    withSourcesJar()
}
