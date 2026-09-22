val projectGroup: String by project
val springBootVersion: String by project
val jakartaValidationApiVersion: String by project
val resilience4jVersion: String by project

plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = projectGroup
            artifactId = "core"
            version = project.version.toString()
        }
    }
}

dependencies {
    api(project(":common"))
    api("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    api("jakarta.validation:jakarta.validation-api:$jakartaValidationApiVersion")
    api("io.github.resilience4j:resilience4j-all:$resilience4jVersion")
    api("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    api("org.springframework.boot:spring-boot-starter-aspectj:$springBootVersion")
}

java {
    withJavadocJar()
    withSourcesJar()
}
