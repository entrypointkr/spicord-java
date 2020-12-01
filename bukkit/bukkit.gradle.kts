import kr.entree.spigradle.kotlin.spigot

plugins {
    java
    id("kr.entree.spigradle") version "2.2.3"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "kr.entree"
version = rootProject.version

repositories {
    jcenter()
}

configurations {
    testImplementation.configure { extendsFrom(compileOnly.get()) }
}

val lombokVersion = "1.18.16"

dependencies {
    implementation(project(":spicord-core"))
    compileOnly(spigot())
    compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

spigot {
    name = "Spicord"
    commands {
        create("spicord")
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    jar {
        dependsOn(shadowJar)
    }
    test {
        useJUnitPlatform()
    }
}
