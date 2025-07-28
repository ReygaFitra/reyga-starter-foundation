plugins {
    `java-library`
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:3.5.3")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.5.3")
}

java {
    withJavadocJar()
    withSourcesJar()
}