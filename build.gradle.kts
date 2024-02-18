plugins {
    id("java")
}

group = "com.golfing8"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.papermc.io/repository/maven-public/")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("org.projectlombok:lombok:1.18.24")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly(group = "com.golfing8", name = "KCommon", version = "1.0").isChanging = true
}

val deployDirectory = "C:\\Users\\Miner\\Desktop\\Server-1.20.2\\plugins"
tasks.create("deploy") {
    dependsOn(tasks.jar)

    doFirst {
        val outputFile = tasks.getByName("jar").outputs.files.first()
        val targetFile = File(deployDirectory, "KCharms-1.0.jar")

        outputFile.copyTo(targetFile, overwrite = true)
    }
}

tasks.test {
    useJUnitPlatform()
}