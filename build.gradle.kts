plugins {
	id("java")
}

group = "net.callisto"
version = "1.0.0"

repositories {
	mavenCentral()
}

dependencies {
	testImplementation(platform("org.junit:junit-bom:5.10.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
	useJUnitPlatform()
}

tasks.jar {
	manifest {
		attributes(mapOf("Main-Class" to "net.callisto.Main"))
	}
	archiveFileName = "${archiveBaseName.get()}.${archiveExtension.get()}"
}
