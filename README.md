# 26.1.2 Multiloader Template + Extras
### Features:
- Fabric/Neoforge support
- Mixin Config plugin with automatic conditional loading based on package
- Debug logging + Multiple client runs for Neoforge
- Publishing to CurseForge, Modrinth, GitHub - *(via modmuss50 publish plugin ([Link](https://github.com/modmuss50/mod-publish-plugin)))*
- FzzyConfig ([Link](https://www.curseforge.com/minecraft/mc-mods/fzzy-config))
- MixinSquared ([Link](https://github.com/Bawnorton/MixinSquared))
- MixinMCP ([Link](https://github.com/muon-rw/MixinMCP)) - *(Can be easily removed)*
___
## Setup:
### 1. Rename your project
- Run the `renameProject` task, passing your mod ID and Mod Name - Mod Name should match the project folder name exactly!:
  ```
  ./gradlew renameProject -PmodId=mymod -PmodName=MyMod
  ```
- Optional properties:
  - `-PmodName=MyMod` — PascalCase display name (defaults to a PascalCase version of `modId`)
  - `-Ppackage=com.example.mymod` — full Java package (defaults to `dev.muon.<modId>`)
  - `-PpackageSuffix=mymod` — shortcut for `dev.muon.<packageSuffix>`. Ignored if `-Ppackage` is also set.
- The task updates `gradle.properties`, `settings.gradle`, mixin JSON files, `fabric.mod.json`, all Java packages/class names, service provider files, and deletes the Fabric run configurations so they regenerate correctly on the next Gradle sync
- **IMPORTANT** The name in `settings.gradle` should match the folder name exactly, case sensitive — rename the folder to match your new mod name if needed

### 2. Download a Java 25 SDK and configure it in IntelliJ:

- `File -> Project Structure -> SDK`

### 3. For good measure make sure this is also set for Gradle:

- `Settings -> Build, Execution, and Deployment -> Build Tools -> Gradle -> Gradle JVM`
- - (*Keep `Build and Run using` set to `Gradle`, not IDEA! This is required for property expansion.*)

### 4. This template requires a local build of MixinMCP in your local maven (For now!)
- See https://github.com/muon-rw/MixinMCP, clone and run `build` + `publishToMavenLocal` (Also recommended, `buildPlugin` then copy from `build/distributions`, Intellij -> Plugins -> Install Plugin from Disk)
- You can simply remove the gradle plugin in the 4 `build.gradle` files where it is present if you do not plan to use this


## Using:
- Run `Fabric Client`, `Fabric Server`, `Neoforge Client`, `Neoforge ClientExtra`, or `Neoforge Server` from your run configurations
- For servers, you will have to set `eula.txt` to true, then `online-mode=false` in `server.propeties` to connect (unless you set up authentication - not covered here)

## Publishing (optional):
Publishing to CurseForge, Modrinth, GitHub, and a Maven repository is pre-wired but gated on the properties below. Fill in only the ones you want to use — each platform is skipped if its ID properties are left blank.

### CurseForge, Modrinth, Github:
1. Set in `gradle.properties`:
- `curseforge_id`: CurseForge Project ID - *Usually a 6-7 digit number, visible on right side of project page*
- `modrinth_id`: Modrinth Project ID - *Obtain from **More Options** in topright corner of project page, **Copy ID***
- `github_owner` + `github_repo`: Both must be set - *Constructed as `https://www.github.com/github_owner/github_repo`*

2. Set these environment variables on your user/system:
- `CURSEFORGE_TOKEN` - Generate [Here](https://legacy.curseforge.com/account/api-tokens) - *Only required if `curseforge_id` is set*
- `MODRINTH_TOKEM` - Generate [Here](https://modrinth.com/settings/pats) - *Only required if `modrinth_id` is set*
- `GITHUB_TOKEN` - Generate [Here](https://github.com/settings/tokens) - *Only required if `github_owner` + `github_repo` is set*
- - - **WARNING!**
- - *These are sensitive info; anyone can upload files to your projects if they get exposed, which is a massive risk vector!*
- - *Do NOT upload these tokens to your GitHub or put them in any other visible location*. 
- - *Do NOT expose these tokens to LLMs or coding assistants*.
### Maven: 
Set these env vars before running `./gradlew publish`:
- `MAVEN_URL` — your Maven repository URL (e.g. `https://maven.example.com/releases`)
- `MAVEN_USERNAME`, `MAVEN_PASSWORD` — credentials

All fzzy_config dependencies are already declared for Modrinth / CurseForge publishing.

## Common troubleshooting: 
1. Refresh gradle, run the `clean` task, then `downloadAssets`
2. Regenerate NeoForge run configs with the `createLaunchScripts` task
3. Regenerate Fabric run configs by deleting them from `/build/runConfigurations` then refreshing the Gradle project