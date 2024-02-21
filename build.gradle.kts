plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "top.mrxiaom"
version = "1.5.7"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.fastmirror.net/repositories/minecraft")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.1")
    implementation("org.jetbrains:annotations:19.0.0")
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
        relocate("org.intellij.lang.annotations", "sandtechnology.redpacket.util.annotations.intellij")
        relocate("org.jetbrains.annotations", "sandtechnology.redpacket.util.annotations.jetbrains")
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
