plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "top.mrxiaom"
version = "1.5.7"

repositories {
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.helpch.at/releases/")
    maven("https://jitpack.io/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.1")
    implementation("com.github.technicallycoded:FoliaLib:0.4.4")
    implementation("org.jetbrains:annotations:24.0.0")
}

val targetJavaVersion = 8
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        mapOf(
            "org.intellij.lang.annotations" to "annotations.intellij",
            "org.jetbrains.annotations" to "annotations.jetbrains",
            "com.tcoded.folialib" to "folialib",
        ).forEach { (original, target) ->
            relocate(original, "sandtechnology.redpacket.util.$target")
        }
    }
    build {
        dependsOn(shadowJar)
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("LICENSE")
        from(sourceSets.main.get().resources.srcDirs) {
            expand(mapOf(
                "version" to version,
            ))
            include("plugin.yml")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenRelease") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
}
