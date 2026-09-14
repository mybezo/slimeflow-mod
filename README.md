# SlimeFlow

SlimeFlow is a client-side utility mod for Minecraft that automates repetitive
inventory management on Slimefun servers — moving items into machine GUIs,
running the same transfer every time a menu opens, and climbing multi-level
machine stacks without doing it by hand.

## Features

- **Macro profiles** — record item → slot → amount actions for any GUI. Save
  a profile once and it can auto-run every time that GUI's title matches
  again.
- **Multiple action types** — move a fixed amount of an item, click a raw
  slot, pick from a list of possible items, or collect an output slot back
  into your inventory (once, or continuously while the GUI is open).
- **Stack automation** — select a column of machines and let SlimeFlow climb
  from one to the next, opening each GUI and running its macro automatically.
- **Auto Login** — queue chat commands or actions to run automatically after
  joining a server.
- **Auto Sell** — trigger a sell action on a configurable interval whenever
  your inventory is full.
- **Backpack refill** — automatically top off a configured item from a
  backpack/ender chest while a macro runs.
- **Fully rebindable keys** and an on-screen keybind editor — no config file
  editing required.

## Requirements

- Minecraft 26.1.2
- [Fabric Loader](https://fabricmc.net/) 0.19.2+
- [Fabric API](https://modrinth.com/mod/fabric-api) for 26.1.2
- Java 25+

## Installation

1. Install Fabric Loader for Minecraft 26.1.2.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 26.1.2 and
   place it in your `mods` folder.
3. Place the SlimeFlow jar in the same `mods` folder.
4. Launch the game with the Fabric profile.

## Usage

| Key | Action |
| --- | --- |
| `F12` | Open / close the SlimeFlow menu |
| `F10` | Stop all running macros and automation immediately |
| `F9` | Start / stop stack automation |

All keys can be rebound from the in-game **Keys** panel.

**Creating a macro:**
1. Open a GUI (chest, machine, etc.) and press `F12`.
2. Open **Macro → Create**, give it a name.
3. Pick a mode (Move / Item / Click / Multi / Output), select the source and
   target, set the amount, and add it as an action.
4. Save. The profile now runs automatically whenever a GUI with that title
   opens.

**Stack automation:**
1. Select the machines to visit, in order, from the **Macro** panel.
2. Press `F9` (or use the Start button) to begin. SlimeFlow moves between the
   selected positions and runs the matching macro at each one.
3. Press `F10` at any time to stop.

## Building from source

```
gradle build
```

Output jar is written to `build/libs/`.

## Disclaimer

This mod automates in-game actions on your behalf. Using automation tools is
against the rules on many servers — check your server's terms before using
SlimeFlow, and use it at your own risk.

## License

All rights reserved. Redistribution or modification without permission from
the author is not allowed unless stated otherwise.
