val projectGroup: String by project
val ojdbcVersion: String by project
val springFrameworkVersion: String by project
val springDataCommonsVersion: String by project

plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = projectGroup
            artifactId = "common-database"
            version = project.version.toString()
        }
    }
}

dependencies {
    api(project(":common"))
    api("org.springframework.data:spring-data-commons:$springDataCommonsVersion")
    implementation("com.oracle.database.jdbc:ojdbc11:$ojdbcVersion")
    api("org.springframework:spring-jdbc:$springFrameworkVersion")
}

java {
    withJavadocJar()
    withSourcesJar()
}
