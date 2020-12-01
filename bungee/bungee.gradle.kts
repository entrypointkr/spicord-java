import kr.entree.spigradle.kotlin.bungeecord

plugins {
    java
    id("kr.entree.spigradle.bungee") version "2.2.3"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "kr.entree"
version = rootProject.version

repositories {
    jcenter()
}

val lombokVersion = "1.18.16"

dependencies {
    implementation(project(":spicord-core"))
    compileOnly(bungeecord())
    compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

bungee {
    name = "Spicord"
}