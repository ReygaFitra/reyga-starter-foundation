val projectGroup: String by project
val apacheTikaVersion: String by project
val owaspSanitizerVersion: String by project
val jasperReportsVersion: String by project
val springFrameworkVersion: String by project

plugins {
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = projectGroup
            artifactId = "common-io"
            version = project.version.toString()
        }
    }
}

dependencies {
    api("org.springframework:spring-web:$springFrameworkVersion")
    api("org.apache.tika:tika-core:$apacheTikaVersion")
    api("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:$owaspSanitizerVersion")
    api("net.sf.jasperreports:jasperreports:$jasperReportsVersion")
}

java {
    withJavadocJar()
    withSourcesJar()
}
