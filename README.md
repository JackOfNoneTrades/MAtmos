> This repository is a fork of [makamys/MAtmos](https://github.com/makamys/MAtmos).

[![downloads](https://img.shields.io/badge/-⬇%20releases-brightgreen)](https://github.com/makamys/MAtmos/releases)
[![builds](https://img.shields.io/badge/-🛈%20builds-blue)](https://makamys.github.io/docs/CI-Downloads/CI-Downloads.html)
[![CurseForge](https://shields.io/badge/CurseForge-555555?logo=curseforge)](https://legacy.curseforge.com/minecraft/mc-mods/matmos)
[![modrinth](https://shields.io/badge/modrinth-555555?logo=data:image/svg+xml;base64,PHN2ZyBmaWxsPSJub25lIiBhcmlhLWxhYmVsPSJtb2RyaW50aCIgY2xhc3M9InNtYWxsLWxvZ28iIHZlcnNpb249IjEuMSIgaWQ9InN2ZzkwMiIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB2aWV3Qm94PSIwIDAgNTEyIDUxNCI+PHN0eWxlPnBhdGh7ZmlsbDojZmZmO2ZpbGwtb3BhY2l0eToxfTwvc3R5bGU+PHBhdGggZmlsbC1ydWxlPSJldmVub2RkIiBjbGlwLXJ1bGU9ImV2ZW5vZGQiIGQ9Ik01MDMgMzI0QTI1NiAyNTYgMCAxMDEgMjMxaDQzYTIxMiAyMTIgMCAwMTQwOS01MGwtNDIgMTJjLTE5LTQ3LTU4LTgyLTEwNS05NmwtNyA0NGExMjQgMTI0IDAgMDExMCAyMjhsMTEgNDNjNzAtMjkgMTEyLTEwMiAxMDItMTc3bDQyLTExYzUgMjkgMyA1Ny0zIDg0bDQyIDE2eiIgaWQ9InBhdGg4OTgiLz48cGF0aCBjbGFzcz0ibm9ybWFsIiBkPSJNMzIyIDUwNEEyNTYgMjU2IDAgMDEwIDI3NWg0M2EyMTMgMjEzIDAgMDAyMCA3MmwzOC0yMy04LTI0QTE2OCAxNjggMCAwMTI2MyA4OWwtOCA0NGExMjUgMTI1IDAgMDAtMTE5IDE1Nmw0IDEyIDQ5LTMwLTE1LTM5IDQ3LTQ4IDU5LTEzIDE3IDIxLTI4IDI4LTIzIDctMTcgMTggOCAyMyAxNyAxOCAyNC03IDE3LTE4IDM2LTEyIDExIDI1LTM4IDQ2LTYzIDIxLTI5LTMyLTUwIDMwYzI2IDI5IDY0IDQ1IDEwNCA0MmwxMiA0M2MtNjAgNy0xMTgtMTctMTU0LTYybC0zOCAyM2EyMTIgMjEyIDAgMDAzNTktMzZsNDMgMTZjLTMxIDY2LTkwIDExOS0xNjYgMTM5eiIgaWQ9InBhdGg5MDAiLz48c3R5bGUvPjwvc3ZnPg==)](https://modrinth.com/mod/matmos)

MAtmos
======

MAtmos is a sound atmosphere generator for Minecraft 1.12 and 1.7.10. Originally created by Hurricaaane, it is now maintained by the community.

Whenever you join a multiplayer server or single-player world, MAtmos will search your surroundings and generates a soundscape of natural noises to match, filling out that perpetual silence of Minecraft.

#### What's new?
```
* New system for detecting when the player is outdoors, indoors, or deep indoors
* A new soundpack making use of the new features: Matmos 2020 "Zen"
* Update checking for sound packs which support it
* Versions are now built for Forge
* Many bug fixes and performance improvements
```

## Soundpacks
MAtmos lets you create your own soundscapes using special resource packs called **soundpacks**. Some of the ones that have been created are:


### MAtmos 2020 "Zen"
[![](http://img.youtube.com/vi/3F85g3e2_MY/0.jpg)](http://www.youtube.com/watch?v=3F85g3e2_MY "")

The flagship soundpack, featuring seamless loops and indoors/outdoors variations for ambiences. It aims to add high quality sounds that enhance the atmosphere without feeling out of place. (MAtmos 34 or higher required.)

**Download: [GitHub](https://github.com/makamys/MAtmos-2020-Zen) / [CurseForge](https://legacy.curseforge.com/minecraft/texture-packs/matmos-2020-zen)**

*(by makamys)*

### Default soundpack (+introduction)

[![](http://img.youtube.com/vi/Z4Zu4kvyDHU/0.jpg)](http://www.youtube.com/watch?v=Z4Zu4kvyDHU "")

The original MAtmos soundpack from 2012, updated to work on the latest version.

**Download: [GitHub](https://github.com/makamys/MAtmos-2016-Default) / [CurseForge](https://legacy.curseforge.com/minecraft/texture-packs/matmos-2016-default)**

*(by Hurricaaane)*

### Rhapsodia

[![](http://img.youtube.com/vi/4_6xS86hcaI/0.jpg)](https://www.youtube.com/watch?v=4_6xS86hcaI)

Rhapsodia's goal is to enhance the audio experience in Minecraft. 
So, now the rain looks like rain, thunder is thundering hard, or you can drink your potion without sounding like your grand-ma while she eats her soup.

**Download: [GitHub](https://github.com/makamys/rhapsodia)**

*(by [Sarys](https://lnk.bio/sarys))*

### More soundpacks
https://github.com/makamys/MAtmos/wiki/Soundpacks

## Usage
By default, MAtmos does not include any sounds. To get started, move the soundpacks you want to your resource packs folder an enable them.

Pressing F4 opens the MAtmos settings menu which can be used to adjust the volume levels of the individual expansions that make up soundpacks. You can even combine different expansions from different soundpacks! Customize your experience to exactly the way you like it.

<img src="docs/matmos_menu.png" width="600">

## Suggested companion mods

[***Dynamic Surroundings***](https://legacy.curseforge.com/minecraft/mc-mods/dynamic-surroundings) for better footstep sounds. (Yes, it's compatible.) Disable its biome sounds in the config to only get the footsteps.

***[Sound Filters](https://legacy.curseforge.com/minecraft/mc-mods/sound-filters) / [Sound Physics](https://github.com/djpadbit/Sound-Physics) ([1.7.10 fork](https://github.com/mist475/Sound-Physics))*** for reverb and muffling of sounds behind walls.

## Mixin troubleshooting

MAtmos uses Mixin in order to apply some modifications to the game. The regular versions of the mod jars embed Mixin, so MAtmos can run standalone: the 1.7.10 version embeds Mixin 0.7.11, while the 1.12.2 version embeds Mixin 0.8.3. However, this can cause issues with other mods if they provide or require a different version.

If you're getting Mixin errors on launch, try these steps in order:

1. Use the versions of MAtmos ending in `+nomixin`, which do not embed Mixin (but require a different mod to provide the library).
2. Add a Mixin bootstrap mod, such as [MixinBooter](https://legacy.curseforge.com/minecraft/mc-mods/mixin-booter) on 1.12.2 or [UniMixins](https://github.com/LegacyModdingMC/UniMixins) on 1.7.10.

Additionally, there are some mods that use ASM transformers which break when Mixin is present. Mixingasm can be used to fix this. UniMixins includes it on 1.7.10, and on 1.12.2 you can add just its Mixingasm module next to MixinBooter.

## Downloads

Releases for **MAtmos** are available on [the releases tab](https://github.com/makamys/MAtmos/releases).

For **soundpack** downloads, see the *Featured soundpacks* section above.

## Contributing

The project uses Git submodules. Run `git submodule update --init` after checking out to fetch them.

The project can be built using Gradle. It uses an ancient buildscript so it has to be built using Java 8. For example: `JAVA_HOME=/path/to/java8-jdk ./gradlew build`

The project uses a multi-project structure. The subproject for each game version can be found in `projects/<version>` folder. Some subprojects can be built with different targets, add `-Ptarget=foo` to your gradle command to target the `foo` target.

To run in an IDE, use the following program arguments:

`--tweakClass org.spongepowered.asm.launch.MixinTweaker --mixin haddon.mixin.json`

The project has a "roadmap" [here](https://github.com/makamys/MAtmos/wiki/Roadmap).

## Licenses

MAtmos itself is under WTFPLv2. Redistribute/Modify at will.

MAtmos source code requires some custom libraries/classes found at https://github.com/makamys/MC-Commons (WTFPLv2)

The (net.sf.) PracticalXML library (Apache License) is also required to compile versions that include the XML expansion converter:
- net.sf.practicalxml.*
