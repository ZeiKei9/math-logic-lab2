plugins {
    id("java")
}

group = "ru.unlegit"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    implementation("com.github.pengrad:java-telegram-bot-api:7.9.1")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations.runtimeClasspath.get().files.forEach { from(zipTree(it)) }
        manifest.attributes("Main-Class" to "ru.unlegit.cnfprocessor.BotBootstrapper")
    }
}