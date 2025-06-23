import org.gradle.api.credentials.PasswordCredentials

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version("7.1.2")
}

group = "com.golfing8"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.papermc.io/repository/maven-public/")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.projectlombok:lombok:1.18.36")
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    compileOnly(group = "com.golfing8", name = "KCommon", version = "1.0").isChanging = true
}

val deployDirectory = "/home/andrew/Servers/Server-1.21.5/plugins"
tasks.create("deploy") {
    dependsOn(tasks.build)

    doFirst {
        val outputFile = tasks.getByName("shadowJar").outputs.files.first()
        val targetFile = File(deployDirectory, "KCharms-1.0.jar")

        outputFile.copyTo(targetFile, overwrite = true)
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.test {
    useJUnitPlatform()
}