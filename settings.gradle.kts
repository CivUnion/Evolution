/*
 * This file was generated by the Gradle 'init' task.
 */

rootProject.name = "evolution"

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://repo.papermc.io/repository/maven-public/")
	}
}

include(":paper")
project(":paper").name = rootProject.name
