plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.reyga-dev.starter"
            artifactId = "core"
            version = "1.0.0"
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-validation:3.5.3")
    implementation("jakarta.validation:jakarta.validation-api:3.1.1")
    implementation("io.github.resilience4j:resilience4j-all:2.3.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.5.3")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}

java {
    withJavadocJar()
    withSourcesJar()
}