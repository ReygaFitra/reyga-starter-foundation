val projectGroup: String by project
val ojdbcVersion: String by project
val springJdbcVersion: String by project

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
    implementation(project(":common"))
    implementation("com.oracle.database.jdbc:ojdbc11:$ojdbcVersion")
    implementation("org.springframework:spring-jdbc:$springJdbcVersion")
}

java {
    withJavadocJar()
    withSourcesJar()
}
