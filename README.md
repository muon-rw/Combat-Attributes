# 26.1.2 Multiloader Template + Extras
### Features:
- **Fabric**/**Neoforge** support
- **Conditional compat mixins** based on package location
- Debug logging + multiple client runs for multiplayer testing
- Easy testing in production by copying a dev jar to any actual modpack instance
- Publishing to CurseForge, Modrinth, GitHub **with changelogs** - *(via modmuss50 publish plugin ([Link](https://github.com/modmuss50/mod-publish-plugin)))*
- **FzzyConfig** ([Link](https://www.curseforge.com/minecraft/mc-mods/fzzy-config))
- **MixinSquared** ([Link](https://github.com/Bawnorton/MixinSquared))
- **MixinMCP** ([Link](https://github.com/muon-rw/MixinMCP)) - *(Can be easily removed)*
___
## Setup:
### 1. Rename your project
- Run the `renameProject` task, passing your mod ID:
  ```
  ./gradlew renameProject -PmodId=my_mod
  ```
- Optional properties (each is inferred from `modId` underscores if omitted):
  - `-PmodName=MyMod` — PascalCase Java class prefix *(`my_mod` → `MyMod`)*
  - `-PmodDisplayName="My Mod"` — name shown in modmenu / NeoForge mods list *(`my_mod` → `My Mod`)*
  - `-PfolderName=My-Mod` — `rootProject.name` and expected folder name *(`my_mod` → `My-Mod`)*
  - `-Ppackage=com.example.mymod` — full Java package *(defaults to `dev.muon.<modId>`)*
- **IMPORTANT** The `rootProject.name` in `settings.gradle` should match the folder name exactly, case sensitive — rename the folder to match after running the task

### 2. Download a Java 25 SDK and configure it in IntelliJ:

- `File -> Project Structure -> SDK`

### 3. For good measure make sure this is also set for Gradle:

- `Settings -> Build, Execution, and Deployment -> Build Tools -> Gradle -> Gradle JVM`
- - (*Keep `Build and Run using` set to `Gradle`, not IDEA! This is required for property expansion.*)

### 4. This template requires a local build of MixinMCP in your local maven (For now!)
- See https://github.com/muon-rw/MixinMCP, clone and run `build` + `publishToMavenLocal` (Also recommended, `buildPlugin` then copy from `build/distributions`, Intellij -> Plugins -> Install Plugin from Disk)
- You can simply remove the gradle plugin in the 4 `build.gradle` files where it is present if you do not plan to use this


## How to Use:
- Run `Fabric Client`, `Fabric ClientExtra`, `Fabric Server`, `Neoforge Client`, `Neoforge ClientExtra`, or `Neoforge Server` from your run configurations
- For servers, you will have to set `eula.txt` to true, then `online-mode=false` in `server.propeties` to connect (unless you set up authentication - not covered here)
- Each release (after setting up publishing below): 
- - Set a version in `gradle.properties` and add to the `CHANGELOG.md`
- - Run `build`, then `publishMods` for CurseForge/Modrinth/GitHub
- - Run `publish` to upload artifacts to your configured Maven

## Publishing:
Publishing to CurseForge, Modrinth, GitHub, and a Maven repository is pre-wired but gated on the properties below. Fill in only the ones you want to use — each platform is skipped if its ID properties are left blank.

### CurseForge, Modrinth, Github:
1. Set in `gradle.properties`:
- `curseforge_id`: CurseForge Project ID - *Usually a 6-7 digit number, visible on right side of project page*
- `modrinth_id`: Modrinth Project ID - *Obtain from **More Options** in topright corner of project page, **Copy ID***
- `github_owner` + `github_repo`: Both must be set - *Constructed as `https://www.github.com/github_owner/github_repo`*

2. Set these environment variables on your user/system before running `./gradlew publishMods`:
- `CURSEFORGE_TOKEN` - Generate [Here](https://legacy.curseforge.com/account/api-tokens) - *Only required if `curseforge_id` is set*
- `MODRINTH_TOKEM` - Generate [Here](https://modrinth.com/settings/pats) - *Only required if `modrinth_id` is set*
- `GITHUB_TOKEN` - Generate [Here](https://github.com/settings/tokens) - *Only required if `github_owner` + `github_repo` is set*
- - - **WARNING!**
- - *These are sensitive info; anyone can upload files to your projects if they get exposed, which is a massive risk vector!*
- - *Do NOT upload these tokens to your GitHub or put them in any other visible location*. 
- - *Do NOT expose these tokens to LLMs or coding assistants*.

**If you add a required dependency, make sure to update all three places:**
1. `fabric.mod.json` *uses modid*
2. Neoforge `mods.toml`- *uses modid*
3. Each `publishMods` "required" section - *uses curseforge/modrinth friendly name slug* 
- *fzzy_config* is already declared as required in these locations.

### Maven: 
Set these env vars before running `./gradlew publish`:
- `MAVEN_URL` — your Maven repository URL (e.g. `https://maven.example.com/releases`)
- `MAVEN_USERNAME`, `MAVEN_PASSWORD` — credentials

## Common troubleshooting: 
1. Refresh gradle, run the `clean` task, then `downloadAssets`
2. Regenerate NeoForge run configs with the `createLaunchScripts` task
3. Regenerate Fabric run configs by deleting them from `/build/runConfigurations` then refreshing the Gradle project