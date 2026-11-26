# Voxira Client

Ein schlanker PvP- und QoL-Client als **Fabric-Mod** für **Minecraft 26.3**.

## Features

| Modul | Kategorie | Beschreibung |
|---|---|---|
| FPS | HUD | Aktuelle Frames pro Sekunde |
| CPS | HUD | Klicks pro Sekunde (links / rechts) |
| Koordinaten | HUD | Position und Blickrichtung |
| Keystrokes | HUD | WASD, Maus und Leertaste mit CPS |
| Armor Status | HUD | Rüstung und Item in der Hand mit Haltbarkeit |
| Zoom | Visual | Taste **C** halten, Mausrad ändert die Stärke |
| Fullbright | Visual | Volle Helligkeit, ohne Gamma-Hack |
| Custom Main Menu | Utility | Eigenes Voxira-Hauptmenü |

- **Mod-Menü:** `Right Shift`. Module per Klick an- und ausschalten, nach Kategorie filtern.
- **HUD-Editor:** im Mod-Menü auf „HUD bearbeiten“ klicken und die Module per Drag & Drop verschieben. Sie rasten an Rändern und an der Bildschirmmitte ein.
- **Config:** `.minecraft/config/voxira.json`, wird automatisch gespeichert.

## Installation

1. [Fabric Loader](https://fabricmc.net/use/) für Minecraft 26.3 installieren
2. [Fabric API](https://modrinth.com/mod/fabric-api) in den Ordner `mods` legen
3. `voxira-client-<version>.jar` (aus den [Releases](../../releases) oder den Actions-Artefakten) ebenfalls nach `mods`
4. Mit dem Fabric-Profil starten

## Entwicklung

Voraussetzung: **JDK 25**.

```bash
./gradlew build        # JAR liegt danach in build/libs/
./gradlew runClient    # Minecraft mit dem Mod starten
```

### Struktur

```
src/main/java/dev/voxira/client/
├── VoxiraClient.java      Einstiegspunkt: Keybinds, Tick, HUD-Registrierung
├── config/                JSON-Config
├── gui/                   Hauptmenü, Mod-Menü, HUD-Editor, Buttons, Theme
├── mixin/                 Hooks in Minecraft (Zoom, Fullbright, CPS, Hauptmenü)
├── module/                Modul-System
│   ├── hud/               HUD-Module (verschiebbar)
│   ├── visual/            Zoom, Fullbright
│   └── utility/           Custom Main Menu
└── util/                  CPS-Tracker
```

### Eigenes Modul hinzufügen

1. Eine Klasse anlegen, die von `Module` erbt (für HUD-Elemente von `HudModule`)
2. Das Modul in `ModuleManager` per `add(new DeinModul())` registrieren

Danach erscheint es automatisch im Mod-Menü, und sein Zustand wird in der Config gespeichert.

## Lizenz

[GPL-3.0](LICENSE). Nicht offiziell von Mojang oder Microsoft.
