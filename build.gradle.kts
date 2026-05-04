object BuildConfig {
    val JAVA_VERSION: Int = 25

    val MINECRAFT_VERSION_RANGE: String = ">=26.1" // range: ">=26.1 <27.1"
    val MINECRAFT_VERSION_MIN: String = MINECRAFT_VERSION_RANGE.split(" ")[0].replace(Regex("^[><=!\\[\\]()]+"), "")
    val MINECRAFT_VERSION: String = "26.1.2"
    val FABRIC_LOADER_VERSION: String = "0.19.2"
    val FABRIC_API_VERSION: String = "0.147.0+26.1.2"

    // https://semver.org/
    var MOD_VERSION: String = "0.1.0"

    var CONFIG_VERSION: String = "3.7.3"
}

plugins {
    id("java-library")
    id("net.fabricmc.fabric-loom") version("1.16.+")
}

base {
    archivesName = "blockhider"
}

group = "kr.pyke"
version = createVersionString()

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${BuildConfig.MINECRAFT_VERSION}")
    implementation("net.fabricmc:fabric-loader:${BuildConfig.FABRIC_LOADER_VERSION}")
    implementation("net.fabricmc.fabric-api:fabric-api:${BuildConfig.FABRIC_API_VERSION}")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("com.electronwill.night-config:toml:${BuildConfig.CONFIG_VERSION}")
    include("com.electronwill.night-config:toml:${BuildConfig.CONFIG_VERSION}")
    implementation("com.electronwill.night-config:core:${BuildConfig.CONFIG_VERSION}")
    include("com.electronwill.night-config:core:${BuildConfig.CONFIG_VERSION}")
}

tasks {
    processResources {
        val propertiesMap = mapOf(
            "version" to version,
            "minecraft_version" to BuildConfig.MINECRAFT_VERSION_MIN
                .replace("(?<=\\D)-".toRegex(), "."), // fabric snapshot test
            "fabric_loader_version" to BuildConfig.FABRIC_LOADER_VERSION,
            "fabric_api_version" to BuildConfig.FABRIC_API_VERSION
        )
        inputs.properties(propertiesMap)
        filesMatching(listOf("fabric.mod.json")) {
            expand(propertiesMap)
        }
    }

    jar {
        //from("LICENSE")
        destinationDirectory.set(layout.buildDirectory.dir("mods"))
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(BuildConfig.JAVA_VERSION)

    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(BuildConfig.JAVA_VERSION)
}

tasks.test {
    useJUnitPlatform()
}

fun createVersionString(): String {
    val builder = StringBuilder()

    val isReleaseBuild = project.hasProperty("build.release")
    val buildId = System.getenv("GITHUB_RUN_NUMBER")

    if (isReleaseBuild) {
        builder.append(BuildConfig.MOD_VERSION)
    } else {
        builder.append(BuildConfig.MOD_VERSION.substringBefore('-'))
        builder.append("-snapshot")
    }

    builder.append("+mc").append(BuildConfig.MINECRAFT_VERSION)

    if (!isReleaseBuild) {
        if (buildId != null) {
            builder.append("-build.${buildId}")
        }
        else {
            builder.append("-local")
        }
    }

    return builder.toString()
}
