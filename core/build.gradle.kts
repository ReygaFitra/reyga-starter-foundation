plugins {
    `java-library`
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-validation:3.5.3")
    implementation("jakarta.validation:jakarta.validation-api:3.1.1")
    implementation("io.github.resilience4j:resilience4j-all:2.3.0")
}

java {
    withJavadocJar()
    withSourcesJar()
}