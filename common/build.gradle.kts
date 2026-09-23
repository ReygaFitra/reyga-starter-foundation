val projectGroup: String by project
val springBootVersion: String by project
val mapstructVersion: String by project
val lombokMapstructBindingVersion: String by project
val jacksonDatabindVersion: String by project
val jakartaPersistenceApiVersion: String by project

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
    api("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    api("jakarta.persistence:jakarta.persistence-api:$jakartaPersistenceApiVersion")
    api("org.mapstruct:mapstruct:$mapstructVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")
}

java {
    withJavadocJar()
    withSourcesJar()
}
