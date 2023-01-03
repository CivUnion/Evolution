import io.papermc.paperweight.util.set

plugins {
	`java-library`
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

dependencies {
	paperDevBundle("1.18.2-R0.1-SNAPSHOT")

    compileOnly("net.civmc.civmodcore:CivModCore:2.4.0:dev-all")
	implementation("net.objecthunter:exp4j:0.4.8")
}
