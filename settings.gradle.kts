rootProject.name = "spicord"

include("core", "bukkit", "bungee")

rootProject.children.forEach { project ->
    project.buildFileName = "${project.name}.gradle.kts"
    project.name = "spicord-${project.name}"
}
