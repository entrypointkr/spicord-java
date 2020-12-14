plugins {
    `java-library`
}

group = "kr.entree"
version = rootProject.version

repositories {
    jcenter()
}

val lombokVersion = "1.18.16"

dependencies {
    api("io.vavr:vavr:0.10.3")
    api("net.dv8tion:JDA:4.2.0_211")
    api("org.yaml:snakeyaml:1.27")
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    compileOnly("org.jetbrains:annotations:20.1.0")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks {
    test {
        useJUnitPlatform()
        systemProperties(project.properties.filter { (key, _) ->
            key.startsWith("spicord")
        })
    }
}
