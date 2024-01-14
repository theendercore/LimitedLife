@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom") version "1.3.8"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.teamvoided.iridium") version "3.1.9"
}

group = project.properties["maven_group"]!!
version = project.properties["mod_version"]!!
base.archivesName.set(project.properties["archives_base_name"] as String)
description = "liminal life"
val modid: String by project
val server_translations: String by project
val player_data_api: String by project

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://maven.nucleoid.xyz") }
        filter {
            includeGroup("xyz.nucleoid")
            includeGroup("eu.pb4")
        }
    }
}

modSettings {
    modId(modid)
    modName("Liminal Life")

    entrypoint("main", "org.teamvoided.liminal_life.LiminalLife::commonInit")
}

dependencies {
    modImplementation(include("xyz.nucleoid", "server-translations-api", server_translations))
    modImplementation(include("eu.pb4", "player-data-api", player_data_api))
}

tasks {
    val targetJavaVersion = 17
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = targetJavaVersion.toString()
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(JavaVersion.toVersion(targetJavaVersion).toString()))
        withSourcesJar()
    }
}