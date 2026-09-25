val projectGroup: String by project
val springBootVersion: String by project
val springFrameworkVersion: String by project
val jakartaPersistenceApiVersion: String by project
val jakartaValidationApiVersion: String by project
val mapstructVersion: String by project
val springDataCommonsVersion: String by project
val resilience4jVersion: String by project
val apacheTikaVersion: String by project
val owaspSanitizerVersion: String by project
val jasperReportsVersion: String by project
val logbackClassicVersion: String by project
val ojdbcVersion: String by project
val okHttpVersion: String by project

plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    api("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    api("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    api("org.springframework.boot:spring-boot-starter-aspectj:$springBootVersion")

    api("org.springframework:spring-web:$springFrameworkVersion")
    api("org.springframework:spring-jdbc:$springFrameworkVersion")
    api("org.springframework.data:spring-data-commons:$springDataCommonsVersion")

    api("jakarta.persistence:jakarta.persistence-api:$jakartaPersistenceApiVersion")
    api("jakarta.validation:jakarta.validation-api:$jakartaValidationApiVersion")
    api("org.mapstruct:mapstruct:$mapstructVersion")
    api("io.github.resilience4j:resilience4j-all:$resilience4jVersion")
    api("ch.qos.logback:logback-classic:$logbackClassicVersion")

    api("org.apache.tika:tika-core:$apacheTikaVersion")
    api("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:$owaspSanitizerVersion")
    api("net.sf.jasperreports:jasperreports:$jasperReportsVersion")
    api("net.sf.jasperreports:jasperreports-pdf:$jasperReportsVersion")
    api("net.sf.jasperreports:jasperreports-json:$jasperReportsVersion")
    api("net.sf.jasperreports:jasperreports-excel-poi:$jasperReportsVersion")
    api("com.squareup.okhttp3:okhttp-jvm:$okHttpVersion")

    api("com.oracle.database.jdbc:ojdbc11:$ojdbcVersion")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = projectGroup
            artifactId = "foundation-dependencies"
            version = project.version.toString()

            pom {
                name.set("Reyga Starter Foundation Dependencies")
                description.set(
                    "Opt-in bundle of third-party production dependencies used by Reyga Starter Foundation"
                )
            }
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}
