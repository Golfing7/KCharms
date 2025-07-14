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
    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }
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
    compileOnly(group = "com.golfing8", name = "KCommon", version = "1.1").isChanging = true

    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.4") {
        exclude("org.spigotmc")
    }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.15") {
        exclude("org.spigotmc")
    }
}

val deployDirectory = "/home/andrew/Servers/Server-1.21.4/plugins"
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