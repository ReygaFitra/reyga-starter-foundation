plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.reyga-dev.starter"
            artifactId = "common"
            version = "1.0.0"
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.5.3")
}

java {
    withJavadocJar()
    withSourcesJar()
}