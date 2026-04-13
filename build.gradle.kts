plugins {
    kotlin("jvm") version "1.9.23" apply false
    kotlin("plugin.serialization") version "1.9.23" apply false
    id("io.ktor.plugin") version "2.3.12" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    dependencies {
        val coroutinesVersion = "1.8.0"
        val koinVersion = "3.5.6"

        add("implementation", kotlin("stdlib"))
        
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
        add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

        // Koin DI
        add("implementation", "io.insert-koin:koin-core:$koinVersion")

        // Testing
        add("testImplementation", platform("org.junit:junit-bom:5.10.0"))
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testImplementation", "org.jetbrains.kotlin:kotlin-test")
        add("testImplementation", "io.mockk:mockk:1.13.10")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
