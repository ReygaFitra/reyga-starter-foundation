val projectGroup: String by project
val springBootVersion: String by project
val jakartaValidationApiVersion: String by project
val resilience4jVersion: String by project
val caffeineVersion: String by project

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
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationApiVersion")
    implementation("io.github.resilience4j:resilience4j-all:$resilience4jVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")
    implementation("org.springframework.boot:spring-boot-starter-aspectj:${springBootVersion}")
}

java {
    withJavadocJar()
    withSourcesJar()
}
