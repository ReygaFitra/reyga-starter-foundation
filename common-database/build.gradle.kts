plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.reyga-dev.starter"
            artifactId = "common-database"
            version = "1.0.0"
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation("com.oracle.database.jdbc:ojdbc11:23.7.0.25.01")
    implementation("org.springframework:spring-jdbc:6.2.9")
}

java {
    withJavadocJar()
    withSourcesJar()
}