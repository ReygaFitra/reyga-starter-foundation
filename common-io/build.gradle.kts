val projectGroup: String by project
val apacheTikaVersion: String by project
val owaspSanitizerVersion: String by project
val jasperReportsVersion: String by project

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
    implementation(project(":common"))
    implementation("org.apache.tika:tika-core:$apacheTikaVersion")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:$owaspSanitizerVersion")
    
    // Jasper minimal interfaces for the API
    compileOnly("net.sf.jasperreports:jasperreports:$jasperReportsVersion")
    testImplementation("net.sf.jasperreports:jasperreports:$jasperReportsVersion")
}

java {
    withJavadocJar()
    withSourcesJar()
}
