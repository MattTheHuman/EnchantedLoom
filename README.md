# EnchantedLoom

A Paper plugin for Minecraft 1.21 that adds a magical loom block providing every banner pattern and dye colour freely — no materials required.

## Features

- Custom **Enchanted Loom** block item with an enchantment glint and configurable lore
- **GUI-based** pattern and dye picker with pagination
- **Save & retrieve** banner designs per player, persisted to `banners.yml`
- **Command-line banner generation** with full tab-completion
- Fully configurable messages, GUI title, item appearance, and layer limits

## Requirements

- Java 21+
- Paper 1.21.x

## Building

```bash
mvn package
```

The shaded jar will be output to `target/EnchantedLoom-1.0.0.jar`.

## Installation

1. Copy the jar into your server's `plugins/` folder.
2. Restart the server.
3. Edit `plugins/EnchantedLoom/config.yml` as needed and run `/enchantedloom reload` (or restart) to apply changes.

## Commands

| Command | Description | Alias |
|---------|-------------|-------|
| `/enchantedloom <open\|give\|banner>` | Root command | `/eloom` |
| `/elopen [player]` | Open the Enchanted Loom GUI | — |
| `/elgive [player] [amount]` | Give an Enchanted Loom block | — |
| `/elbanner <base_color> [pattern:color ...] [player]` | Create a patterned banner | — |

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
| `enchantedloom.admin` | Grants all of the above | op |

## Configuration

Key options in `config.yml`:

| Key | Default | Description |
|-----|---------|-------------|
| `loom-name` | `&5&lEnchanted Loom` | Display name of the item |
| `use-custom-gui` | `true` | Open custom GUI on right-click |
| `patterns-per-page` | `27` | Patterns shown per GUI page |
| `max-banner-layers` | `6` | Maximum layers on a banner |
| `item-glow` | `true` | Enchantment glint on the item |
