plugins {
    `java-library`
}

val springBootVersion: String by project
val resilience4jVersion: String by project
val caffeineVersion: String by project
val apacheTikaVersion: String by project
val owaspSanitizerVersion: String by project
val jasperReportsVersion: String by project

dependencies {
    api(project(":common"))
    api(project(":common-database"))
    api(project(":common-io"))
    api(project(":core"))
    api(project(":logging"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-aspectj:$springBootVersion")
    implementation("io.github.resilience4j:resilience4j-all:$resilience4jVersion")
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")
    implementation("org.apache.tika:tika-core:${apacheTikaVersion}")
    implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:${owaspSanitizerVersion}")

    implementation("net.sf.jasperreports:jasperreports:$jasperReportsVersion")
    implementation("net.sf.jasperreports:jasperreports-pdf:$jasperReportsVersion")
    implementation("net.sf.jasperreports:jasperreports-json:$jasperReportsVersion")
    implementation("net.sf.jasperreports:jasperreports-excel-poi:$jasperReportsVersion")
}

java {
    withJavadocJar()
    withSourcesJar()
}
