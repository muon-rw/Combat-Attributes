# 26.1.2 Multiloader Template + Extras
### Features:
- Fabric/Neoforge support
- Debug logging+second client run for Neoforge
- FzzyConfig
- MixinSquared
- MixinMCP ([Link](https://github.com/muon-rw/MixinMCP))
___
## Setup:
### 1. Rename your project
- Run `./gradlew renameProject --console=plain` from the project root
- You will be prompted for:
  - **Mod ID** (lowercase, e.g. `mymod`) — suggestion matches the folder name
  - **Mod name** (PascalCase, e.g. `MyMod`) — suggestion derived from the mod ID
  - **Package suffix** (lowercase, e.g. `mymod`) — suggestion matches the mod ID
- The task updates `gradle.properties`, `settings.gradle`, mixin JSON files, `fabric.mod.json`, all Java packages/class names, service provider files, and deletes the Fabric run configurations so they regenerate correctly on the next Gradle sync
- **IMPORTANT** The name in `settings.gradle` should match the folder name exactly, case sensitive — rename the folder to match your new mod name if needed

### 2. Download a Java 25 SDK and configure it in IntelliJ:

- `File -> Project Structure -> SDK`

### 3. For good measure make sure this is also set for Gradle:

- `Settings -> Build, Execution, and Deployment -> Build Tools -> Gradle -> Gradle JVM`
- - (*Keep `Build and Run using` set to `Gradle`, not IDEA! This is required for property expansion.*)

### 4. This template requires a local build of MixinMCP in your local maven (For now!)
- See https://github.com/muon-rw/MixinMCP
- You can simply remove the gradle plugin in the 4 `build.gradle` files where it is present if you do not plan to use this


## Using:
- Run `Fabric Client`, `Fabric Server`, `Neoforge Client`, `Neoforge ClientExtra`, or `Neoforge Server` from your run configurations
- For servers, you will have to set `eula.txt` to true, then change the server to `online-mode=false` if you do not set up authentication, for your clients to connect

## Common troubleshooting: 
1. Refresh gradle, run the `clean` task, then `downloadAssets`
2. Regenerate NeoForge run configs with the `createLaunchScripts` task
3. Regenerate Fabric run configs by deleting them from `/build/runConfigurations` then refreshing the Gradle project