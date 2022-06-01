import net.civmc.civgradle.common.util.civRepo

plugins {
    `java-library`
    `maven-publish`
	id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.civmc.civgradle.plugin") version "1.0.0-SNAPSHOT"
}

// Temporary hack:
// Remove the root build directory
gradle.buildFinished {
	project.buildDir.deleteRecursively()
}

allprojects {
	group = "com.github.longboyy.evolution"
	version = "1.0.0-SNAPSHOT"
	description = "Evolution"
}

subprojects {
	apply(plugin = "net.civmc.civgradle.plugin")
	apply(plugin = "java-library")
	apply(plugin = "maven-publish")
	apply(plugin = "com.github.johnrengelman.shadow")

	java {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(17))
		}
	}

	repositories {
		mavenCentral()
        civRepo("CivMC/CivModCore")
        //civRepo("CivMC/NameLayer")
        //civRepo("CivMC/Citadel")
	}

	publishing {
		repositories {
			maven {
				name = "GitHubPackages"
				url = uri("https://github.com/CivUnion/Evolution")
				credentials {
					username = System.getenv("GITHUB_ACTOR")
					password = System.getenv("GITHUB_TOKEN")
				}
			}
		}
		publications {
			register<MavenPublication>("gpr") {
				from(components["java"])
			}
		}
	}
}
