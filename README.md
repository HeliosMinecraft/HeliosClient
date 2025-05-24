<p align="center">
    <img src="https://github.com/HeliosMinecraft/HeliosClient/blob/main/.github/images/logo.png?raw=true" height="150px">
</p>
<div align="center">
    <h3>Fabric anarchy utility mod for the latest Minecraft version.</h3>
    <img src="https://img.shields.io/github/last-commit/HeliosMinecraft/HeliosClient" alt="GitHub last commit"/>
    <img src="https://img.shields.io/github/commit-activity/w/HeliosMinecraft/HeliosClient" alt="GitHub commit activity"/>
    <img src="https://img.shields.io/github/contributors/HeliosMinecraft/HeliosClient" alt="GitHub contributors"/>
</div>

## Images

<p>Keep in mind that these images might be outdated as Helios is currently updating frequently!</p>
<details>
    <summary>Click to show the latest client GUI</summary>
    <p>WIP</p>
    <img src="https://github.com/HeliosMinecraft/HeliosClient/blob/main/.github/images/Latest_ClickGUI.png?raw=true" alt="Latest Click GUI">
</details>  
<details>
    <summary>Click to show old GUI </summary>
    <p>New clickGui as of commit #235 (a13bf24)</p>
    <img src="https://github.com/HeliosMinecraft/HeliosClient/blob/main/.github/images/Updated_ClickGUI.png?raw=true" alt="New Click GUI">
    <p>Previous click-gui as of commit #199 (0758e8c)</p>
    <img src="https://github.com/HeliosMinecraft/HeliosClient/blob/main/.github/images/heliosclientgui.png?raw=true" alt="Prev Click GUI">
    <p>Very old GUI (In starting stages)</p>
    <img src="https://github.com/HeliosMinecraft/HeliosClient/blob/main/.github/images/ClickGUI.png?raw=true" alt="Old Click GUI">
</details>

## Building

1. Clone this repository
2. Run:
   - Windows (CMD): `gradlew build`
   - Linux/macOS/Windows (Powershell): `./gradlew build`
  
## Installation

- Install [Fabric](https://fabricmc.net/use/installer/) for Minecraft 1.21.4
- Put this mod into the `.minecraft/mods` folder
- The default keybind for the click-gui is `RSHIFT`.

***Note: In future we may require [Satin API](https://modrinth.com/mod/satin-api) for shaders (or it will be packaged together)***


## Submitting a Bug

To submit a bug open an issue in this repository. Before doing so please assure yourself that the issues isn't already listed under [Known issues](#known-issues).

## Known issues
- ~~Buggy modules like Tick-Shift.~~
- Incomplete modules like Phase, Fucker, InventoryTweaks.
- ~~HudElements don't resize to their proper locations sometimes.~~
- Scripting System (WIP).
- Lack of combat and some important utility modules.
- ~~No Baritone Integration~~
- ~~No AltManager~~  No AltManager GUI
- Lack of commands
- Lack of shaders (Neither of the current contributors know much good about GLSL)

## Contributing

If you want to contribute to this project, look into the [`CONTRIBUTING.md`](https://github.com/HeliosMinecraft/HeliosClient/blob/main/CONTRIBUTING.md) file for more detail.

## Contributors

Thanks to all the people who helped to make this project what it is now, especially the main team:

<p align="center">
    <a href="https://github.com/azedeveloper"><img src="https://github.com/azedeveloper.png" width="24%"></img></a> <a href="https://github.com/ElBe-Plaq"><img src="https://github.com/ElBe-Plaq.png" width="24%"></img></a> <a href="https://github.com/tanishisherewithhh"><img src="https://github.com/tanishisherewithhh.png" width="24%"></img></a> <a href="https://github.com/TomPlaneta"><img src="https://github.com/TomPlaneta.png" width="24%"></img></a>
</p>

## Thanks to

- [MoonlightMeadows](https://github.com/kawaiizenbo/MoonlightMeadows) made by [Kawaiizenbo](https://github.com/kawaiizenbo) for serving as base project.
- <a href="https://github.com/RacoonDog">Crosby</a>, <a href="https://github.com/etianl">etianl</a> and <a href="https://github.com/rfresh2">rfresh</a> for the whole Palette Exploit
- Many other clients for serving as a base for inspiration. (Like Meteor-Client, BleachHack, TH Recode, Old 3arth4ck, LiquidBounce)
- [0x3C50](https://github.com/0x3C50/Renderer) for the Renderer library (especially the FontRenderer).
- [Bleach Hack](https://github.com/BleachDev/BleachHack/tree/1.20.4) for the 3D rendering.

## Support
**Join our [discord server](https://discord.gg/zNCnP3pCvx) for additional support or create an issue.**
