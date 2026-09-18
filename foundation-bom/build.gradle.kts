val projectGroup: String by project
val projectVersion: String by project

plugins {
    `java-platform`
    `maven-publish`
}

dependencies {
    constraints {
        api(project(":common"))
        api(project(":common-io"))
        api(project(":common-database"))
        api(project(":core"))
        api(project(":logging"))

        api("$projectGroup:foundation-starter:$projectVersion")
    }
}

publishing {
    publications {
        create<MavenPublication>("foundationBom") {
            from(components["javaPlatform"])

            groupId = projectGroup
            artifactId = "foundation-bom"
            version = projectVersion

            pom {
                name.set("Reyga Starter Foundation BOM")
                description.set(
                    "Dependency version alignment for Reyga Starter Foundation modules"
                )
            }
        }
    }
}