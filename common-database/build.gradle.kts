plugins {
    `java-library`
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