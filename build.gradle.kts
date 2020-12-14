plugins {
    base
}

group = "kr.entree"
version = "0.1.0"

allprojects {
    tasks.withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }
}