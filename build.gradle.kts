plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "com.tfkfan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(19)
}

application {
    mainClass.set("com.tfkfan.MainKt")
}