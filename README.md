
<img align="right" alt="logo" width="130" src="https://cardboardpowered.org/assets/cardboard-box.png">

# Cardboard [![Join Discord](https://img.shields.io/badge/Discord-Join-7289DA?logo=discord&style=flat-square)](https://discord.gg/tddTWXZtaP) <img alt="Fabric" src="https://img.shields.io/badge/Fabric%20-0.16%2B-%23dacfa4">

Cardboard is an implementation of the popular Bukkit/Spigot/Paper Modding API for FabricMC. This mod lets you use plugins that are made for Bukkit and it's derivatives (Spigot & Paper) on a Fabric modded server.

Fabric version chart:
| Support  | Minecraft        | Git Branch  | Dev Status |
|----------|------------------|-------------|------------|
| &#x2705; | Fabric 26.2      | ver/26.2    | Active     |
| &#x2705; | Fabric 26.1.2    | ver/26.1    | Low        |
| &#x2705; | Fabric 1.21.11   | ver/1.21.11 | Low        |
| &#x2705; | Fabric 1.21.1    | ver/1.21    | Low        |
| &#x274C; | <= 1.20          |             |            |

See [Supported Versions](https://github.com/CardboardPowered/cardboard/wiki/Supported-Versions) for more details. & [View Downloads](https://cardboardpowered.org/download/)

## Building

Requires JDK 25 (auto-provisioned by the Gradle toolchain resolver) and Java 21+ at runtime.

```
./gradlew build
```

The mod jar is produced at `build/libs/Cardboard-<version>.jar`. Drop it into a
Fabric server's `mods/` folder alongside [Fabric API](https://modrinth.com/mod/fabric-api)
and iCommonLib; Bukkit plugins then go in `plugins/`.

### 26.2 notes

Minecraft 26.2 moved the entity and block-entity constants out of their type
classes (`EntityType.X` -> `EntityTypes.X`, `BlockEntityType.X` ->
`BlockEntityTypes.X`), stripped the colour metadata from `ChatFormatting` in
favour of `world.scores.TeamColor`, and ships Adventure 5. Cardboard on this
branch targets Paper-API `26.2.build.110-stable` and Adventure `5.2.0`.

## About this fork
This repository is derived from [CardboardPowered/cardboard](https://github.com/CardboardPowered/cardboard)
and carries its history and license. The `ver/26.2` branch adds Minecraft 26.2 support.

## License
We inherit the license from Paper. See [Paper's License](https://github.com/PaperMC/Paper/blob/master/LICENSE.md) for full details.
SrgLib is also licensed under MIT.

<!--
## NMS Support
We do support using Spigot's ``net.minecraft.server`` classes. 
Classes and Fields will automatically remap to their intermediary counterparts.
However, the current system is far from perfect.

# Progress
There is a progress indicator in the Discord. Although, if you're not in the Discord:
Progress can be determined by the completeness of the to-do lists on the two pinned issues.
-->

## Credits
* [BukkitTeam](https://bukkit.org/), [Spigot](https://spigotmc.org/), and [Paper](https://papermc.io/) for their work on the API.
* [Glowstone](https://glowstone.net) for the library loader.
* [md_5's SpecialSource](https://github.com/md-5/SpecialSource), [SrgLib by Techcable & Orion](https://github.com/OrionMinecraft/SrgLib), [MinecraftMapping by Phase](https://github.com/phase/MinecraftMapping/)
* Contributors to Cardboard

# Apex Hosting 
This project is partnered with ApexHosting! Join our test server, `cardboardmod.apexmc.co:25666`, or get a Minecraft server by clicking on the banner below:

[![Apex Hosting](https://cdn.apexminecrafthosting.com/img/theme/apex-hosting-mobile.png)](https://billing.apexminecrafthosting.com/aff.php?aff=3548)
