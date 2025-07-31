plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.reyga-dev.starter"
            artifactId = "logging"
            version = "1.0.0"
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-aop:3.5.3")
}

java {
    withJavadocJar()
    withSourcesJar()
}