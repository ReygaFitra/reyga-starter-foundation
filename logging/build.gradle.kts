plugins {
    `java-library`
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-aop:3.5.3")
}

java {
    withJavadocJar()
    withSourcesJar()
}