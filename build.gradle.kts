plugins {
    id("java")
}

group = "me.sytex"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.helpch.at/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")          // Paper
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.3.0")    // MiniPlaceholders
    compileOnly("me.clip:placeholderapi:2.11.6")                            // PlaceholderAPI
    compileOnly("org.projectlombok:lombok:1.18.38")                         // Lombok
    annotationProcessor("org.projectlombok:lombok:1.18.38")                 // Lombok
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
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
                links("https://jd.papermc.io/paper/1.20.6/")
            }
        }
    }

    build {
        dependsOn(javadoc)
    }
}