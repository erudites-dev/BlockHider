# Fabric Mod Template

## Project Setup

1. Generate the repository using this template
2. Clone the repository to your local machine
3. Open the project in IntelliJ IDEA
4. Replace `rootProject.name` in `settings.gradle.kts` with the name of your mod
5. Replace the `BuildConfig` in `build.gradle.kts` with appropriate values for your mod
6. Sync the Gradle project

## Development

1. Replace the default package structure with `dev.erudites.mods.template` under `src/main/java/`
2. Rename the following entrypoint classes to match your mod's identity:
    - `TemplateMod.java` in `src/main/java/dev/erudites/mods/template/`
    - `TemplateClientMod.java` in `src/main/java/dev/erudites/mods/template/client/`
3. Rename `src/main/resources/template.mixins.json` to match your **modid** (e.g., `modid.mixins.json`) and update the "package" reference inside the file
4. Update `src/main/resources/fabric.mod.json` to reflect your mod's ID, version, entrypoints, and mixin configurations

## Gradle Tasks

- `./gradlew build` - Builds the mod JAR file
- `./gradlew runClient` - Runs a local client for testing the mod
- `./gradlew runServer` - Runs a local server for testing the mod