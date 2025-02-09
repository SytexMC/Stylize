plugins {
    id("java")
    id("maven-publish")
}

group = "me.sytex"
version = "1.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.helpch.at/releases")
}

dependencies {
    implementation("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")

    implementation("io.github.miniplaceholders:miniplaceholders-api:2.2.3")
    implementation("me.clip:placeholderapi:2.11.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks {
    javadoc {
        options {
            (this as StandardJavadocDocletOptions).apply {
                addStringOption("Xdoclint:none", "-quiet")
                addStringOption("encoding", "UTF-8")
                addStringOption("charSet", "UTF-8")
                addBooleanOption("html5", true)
                links("https://docs.oracle.com/en/java/javase/17/docs/api/")
                links("https://jd.papermc.io/paper/1.18.2/")
            }
        }
    }

    build {
        dependsOn(javadoc)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = rootProject.version.toString()
        }
    }
}