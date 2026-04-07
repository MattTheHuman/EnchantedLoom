# EnchantedLoom

A Paper plugin for Minecraft 1.21 that adds a craftable Enchanted Loom — a survival-friendly block that lets players apply any banner pattern using a GUI, without needing to gather pattern items.

> Built using [Claude](https://claude.ai) (Anthropic) for a private Minecraft server.

<p align="center">
  <img src="images/Enchanted%20Loom%20-%201%20-%20GUI.png" alt="Enchanted Loom — Base Colour step" width="340">
</p>

## Features

- **Survival-friendly crafting** — craft a Diamond String and use it to craft the Enchanted Loom
- **Custom GUI** — wizard-style pattern picker with base colour, pattern, and pattern colour steps
- **Blank banner trade** — players must supply a matching blank banner to receive the patterned one (configurable)
- **Creative bypass** — creative mode players are never required to supply a blank banner
- **Save & retrieve designs** — save named banner designs per player, persisted across restarts
- **Global banners** — optionally allow all players (or creative players) to browse everyone's saved designs
- **Vanilla loom untouched** — only the crafted Enchanted Loom opens the custom GUI; regular looms work normally
- **Fully configurable** — messages, GUI title, item appearance, layer limits, and all survival settings
- **Live reload** — reload config without restarting the server

## Requirements

- Java 21+
- Paper 1.21.x

## Crafting

**Step 1 — Diamond String**

Place four diamonds around a piece of string:

```
. D .
D S D
. D .
```
`D` = Diamond, `S` = String

<p align="center">
  <img src="images/Enchanted%20Loom%20-%206%20-%20Diamond%20String.png" alt="Diamond String crafting recipe" width="340">
</p>

**Step 2 — Enchanted Loom**

Place two Diamond Strings across the top and two planks across the bottom:

```
A A
B B
```
`A` = Diamond String, `B` = any Plank

<p align="center">
  <img src="images/Enchanted%20Loom%20-%207%20-%20Crafting.png" alt="Enchanted Loom crafting recipe" width="340">
</p>

## GUI

Right-clicking the Enchanted Loom opens a three-step wizard:

1. **Base Colour** — choose the colour of the blank banner
2. **Add Pattern** — browse every available Minecraft banner pattern; hover for a tooltip preview
3. **Pattern Colour** — pick the colour for that pattern layer, then click to add it

Repeat steps 2–3 to layer up to six patterns, then take the finished banner.

<p align="center">
  <img src="images/Enchanted%20Loom%20-%201%20-%20GUI.png" alt="Step 1: Base Colour" width="260">
  &nbsp;
  <img src="images/Enchanted%20Loom%20-%202%20-%20Border%20Select.png" alt="Step 2: Add Pattern" width="310">
  &nbsp;
  <img src="images/Enchanted%20Loom%20-%203%20-%20Colour%20Select.png" alt="Step 3: Pattern Colour" width="310">
</p>

## Saving Designs

While in the pattern picker, click **Save Design** (the book icon) to name and save your current design via chat. Open the Saved Banners screen at any time to browse your designs — left-click to get a copy, shift-click to delete.

<p align="center">
  <img src="images/Enchanted%20Loom%20-%204%20-%20Save%20Banners.png" alt="Save Design button" width="330">
  &nbsp;&nbsp;
  <img src="images/Enchanted%20Loom%20-%205%20-%20Saved%20Banners.png" alt="Saved Banners list" width="260">
</p>

## Building from source

```bash
mvn package
```

The shaded jar will be output to `target/EnchantedLoom-<version>.jar`.

## Installation

1. Copy the jar into your server's `plugins/` folder.
2. Restart the server.
3. Edit `plugins/EnchantedLoom/config.yml` as needed.
4. Run `/elreload` to apply changes without restarting.

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/eloom open [player]` | Open the Enchanted Loom GUI | `enchantedloom.open` |
| `/eloom give [player] [amount]` | Give an Enchanted Loom block | `enchantedloom.give` |
| `/eloom banner <base> [pattern:color ...] [player]` | Create a patterned banner | `enchantedloom.banner` |
| `/eloom reload` | Reload the configuration | `enchantedloom.admin` |
| `/elopen [player]` | Shorthand for open | `enchantedloom.open` |
| `/elgive [player] [amount]` | Shorthand for give | `enchantedloom.give` |
| `/elbanner <base> [pattern:color ...] [player]` | Shorthand for banner | `enchantedloom.banner` |
| `/elreload` | Shorthand for reload | `enchantedloom.admin` |

### `/elbanner` examples

```
/elbanner red stripe_top:white creeper:gold
/elbanner white gradient:purple gradient_up:light_blue Steve
```

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `enchantedloom.use` | Interact with the Enchanted Loom block | everyone |
| `enchantedloom.open` | Open the GUI | everyone |
| `enchantedloom.give` | Give Enchanted Loom blocks | op |
| `enchantedloom.banner` | Generate banners via command | op |
| `enchantedloom.admin` | Grants all of the above + reload + admin delete | op |

## Configuration

| Key | Default | Description |
|-----|---------|-------------|
| `loom-name` | `&5&lEnchanted Loom` | Display name of the item |
| `loom-lore` | *(see config)* | Lore lines on the item |
| `gui-title` | `&5&lEnchanted Loom` | GUI inventory title |
| `use-custom-gui` | `true` | Open custom GUI on right-click |
| `patterns-per-page` | `27` | Patterns shown per GUI page |
| `max-banner-layers` | `6` | Maximum layers on a banner |
| `item-glow` | `true` | Enchantment glint on the item |
| `require-blank-banner` | `true` | Survival players must trade a blank banner |
| `global-banners` | `false` | All players can see all saved designs |
| `global-creative-banners` | `false` | Creative players can see all saved designs |
| `allow-admin-delete-banners` | `false` | Admins can delete any player's saved design |

All messages are configurable under the `messages:` section of `config.yml`. Missing keys are automatically filled in from the plugin defaults on startup or reload.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
