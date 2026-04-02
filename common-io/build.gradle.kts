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
    implementation("net.sf.jasperreports:jasperreports:$jasperReportsVersion")
    implementation("net.sf.jasperreports:jasperreports-pdf:$jasperReportsVersion")
    implementation("net.sf.jasperreports:jasperreports-json:$jasperReportsVersion")
    implementation("net.sf.jasperreports:jasperreports-excel-poi:$jasperReportsVersion")

}

java {
    withJavadocJar()
    withSourcesJar()
}
