# Combat Attributes

*A clean set of RPG combat attributes for Minecraft, with diminishing returns on the stats that would otherwise spiral out of control.*

For Minecraft 26.1.2 (Fabric / NeoForge). Requires [FzzyConfig](https://modrinth.com/mod/fzzy-config).

___

# Features

**Twenty-one** combat focused attributes, centralized for easy usage by other mods.
*Diminishing Returns built in!*


## Diminishing Returns


> [!IMPORTANT]
> ### How do Diminishing Returns Work?
> Each modifier source is squeezed by `min(x, M*x/(x+k))` (per source cap `M`, half-saturation `k`), then sources combine based on the attribute's `stackingMode`:
> - `SOFT_CAP`: sources sum. Use for additive bonuses like crit damage. *Example:* with `melee_crit_damage` (M=+100%, k=40%), a +50% modifier passes through linearly; a raw +200% bends to ~+83%, asymptoting toward +100% — the per source ceiling.
> - `MULTIPLICATIVE`: reductions diminish per source, then combine multiplicatively via `(1-a)(1-b)...`. Use for "lower is better" multipliers like `stamina_cost`. *Example:* two -20% reductions stack to -36% (not -40%) — each discount applies on top of the previous.
> - `PROBABILISTIC`: sources combine via `1 - (1-a)(1-b)...`. Use for chance stats (crit chance, evasion, accuracy). *Example:* two 40% crit chance sources stack to 64%, three to 78.4% — each additional source is worth less than the last.
>
> `LINEAR` skips diminishing entirely. The three modifier operations (`ADD_VALUE`, `ADD_MULTIPLIED_BASE`, `ADD_MULTIPLIED_TOTAL`) each diminish independently, so addons can't dodge the cap by switching op type.

## Attributes:

| Attribute            | Description                                                                      | Default | Diminishing Returns? | Per source cap |
|----------------------|----------------------------------------------------------------------------------|---------|----------------------|----------------|
| `melee_crit_chance`  | Chance for a melee hit to land a critical strike                                 | 0%      | Yes                  | 50%            |
| `melee_crit_damage`  | Damage multiplier applied on a melee crit                                        | 150%    | No                   | +100%          |
| `ranged_damage`      | Flat damage added to non-magic projectile attacks                                | 0       | No                   | none           |
| `ranged_crit_chance` | Chance for a projectile hit to crit                                              | 0%      | Yes                  | 50%            |
| `ranged_crit_damage` | Damage multiplier applied to ranged crits                                        | 150%    | No                   | +100%          |
| `magic_power`        | Magic damage multiplier, analog to attack damage. Exposed for spell mods to read | 1.0     | No                   | none           |
| `magic_crit_chance`  | Chance for `#c:is_magic` damage to critical strike                               | 0%      | Yes                  | 50%            |
| `magic_crit_damage`  | Damage multiplier applied to magic crits                                         | 150%    | No                   | +100%          |
| `draw_speed`         | Speeds up bow draw and crossbow charge                                           | 0%      | No                   | none           |
| `arrow_velocity`     | Increases launch speed of fired arrows                                           | 0%      | No                   | none           |
| `accuracy`           | Tightens projectile spread, up to perfect accuracy                               | 0%      | Yes                  | 100%           |
| `evasion`            | Chance to dodge incoming damage of any source                                    | 0%      | Yes                  | 30%            |
| `lifesteal`          | Heals attacker for a fraction of damage dealt within attack range                | 0%      | No                   | none           |
| `magic_defense`      | Armor style mitigation against `#c:is_magic` damage                              | 0       | No                   | none           |
| `max_stamina`        | Max stamina pool. Powers sprinting, jumping, attacks, bow draws, etc             | 20      | No                   | none           |
| `stamina_regen`      | Stamina regenerated per second                                                   | 2       | No                   | none           |
| `stamina_cost`       | Multiplier on stamina costs paid by abilities                                    | 100%    | Yes                  | -30%           |
| `max_mana`           | Max mana pool. Exposed for spell and ability mods                                | 20      | No                   | none           |
| `mana_regen`         | Mana regenerated per second                                                      | 1       | No                   | none           |
| `mana_cost`          | Multiplier on mana costs paid by abilities                                       | 100%    | Yes                  | -30%           |
| `experience_gain`    | Multiplier on XP awarded to your XP bar from experience orbs                     | 100%    | No                   | none           |

### Legacy Hunger system:
- Option to **disable vanilla hunger** entirely, to use the stamina system instead *(But you can use both if you want)*
- When Legacy Hunger is enabled, all food instead **restores health**, with a heart amount based on its nutrition and saturation values
- The conversion from food/saturation into hearts restored is configurable!

### Default HUD Bars, Dynamic Resource Bars compatibility:
[Example gifs TODO]


### Compatible with Appleskin! 
[Appleskin Compat]

### Configuration

All attribute presets are fully customizable, server-side and synced to clients (using FzzyConfig):

```
config/combat_attributes/combat_attributes-attributes.toml
config/combat_attributes/combat_attributes-general.toml
```

Each attribute can configure:
- `minValue`, `maxValue`: bounds 
- `defaultValue`: starting value applied to every living entity
- `stackingMode`: `LINEAR`, `SOFT_CAP`, `PROBABILISTIC`, or `MULTIPLICATIVE` *(see [Diminishing Returns](#Diminishing-Returns))*
- `softCap` (M): the per source asymptote (the most a single source can ever contribute) *(see [Diminishing Returns](#Diminishing-Returns))*
- `halfSaturation` (k): how soon the diminishing curve kicks in *(see [Diminishing Returns](#Diminishing-Returns))*

> [!WARNING]
> Due to the nature of Attribute registration, changing these values **requires a game restart** in order to take effect!

`combat_attributes-general.toml` contains settings for:
- Stamina drain: attack, sprint, jump, swim, elytra, bow draw, trident throw, block break), regen lockout windows
- The legacy hunger toggle and heal formula
- HUD visibility toggles
___

<details>
<summary><h1>Mod Developers (Click to Expand)</h1></summary>

### Maven Repository

Add the following mavens to your `repositories` in `build.gradle` (assuming Gradle Groovy):

```groovy
repositories {
    // ... other repositories
    // Combat Attributes
    maven { url = "https://maven.muon.rip/releases/" }
    // FzzyConfig
    maven { url = "https://maven.fzzyhmstrs.me/" }
    // Mixin Squared
    maven { url = "https://maven.bawnorton.com/releases" }
}
```

Set a mod version in `gradle.properties`:

```properties
combat_attributes_version=0.1.0
minecraft_version=26.1.2
fzzy_config=0.7.6+26.1
```

Find all available mod versions at: [maven.muon.rip/#/releases/](https://maven.muon.rip/#/releases/)

Then add the appropriate artifact in `dependencies` in your `build.gradle`.

#### NeoForge (assuming ModDevGradle)

```groovy
implementation("dev.muon.combat_attributes:combat_attributes-neoforge:${combat_attributes_version}+${minecraft_version}")
implementation("me.fzzyhmstrs:fzzy_config:${fzzy_config}+neoforge")
```

#### Fabric (assuming Fabric Loom)

```groovy
implementation("dev.muon.combat_attributes:combat_attributes-fabric:${combat_attributes_version}+${minecraft_version}")
implementation("me.fzzyhmstrs:fzzy_config:${fzzy_config}")
```

#### Common (assuming ModDevGradle)

```groovy
compileOnly("dev.muon.combat_attributes:combat_attributes-common:${combat_attributes_version}+${minecraft_version}")
compileOnly("me.fzzyhmstrs:fzzy_config:${fzzy_config}")
```

For an example, see [Chronicles: Leveling](https://www.github.com/muon-rw/Chronicles-Leveling), which integrates closely with this mod.

</details>
