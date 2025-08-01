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

    api("ch.qos.logback:logback-classic:1.5.18")
    api("org.codehaus.janino:janino:3.1.12")
}

java {
    withJavadocJar()
    withSourcesJar()
}