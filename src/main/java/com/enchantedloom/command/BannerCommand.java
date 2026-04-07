package com.enchantedloom.command;

import com.enchantedloom.EnchantedLoomPlugin;
import com.enchantedloom.gui.EnchantedLoomGUI;
import com.enchantedloom.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Registry;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * /elbanner <base_colour> [pattern:colour ...] [player]
 *
 * <p>Examples:
 * <pre>
 *   /elbanner red stripe_top:white creeper:gold
 *   /elbanner white gradient:purple gradient_up:light_blue Steve
 * </pre>
 */
public class BannerCommand implements CommandExecutor, TabCompleter {

    private final EnchantedLoomPlugin plugin;

    public BannerCommand(EnchantedLoomPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("enchantedloom.banner")) {
            sender.sendMessage(Messages.get("no-permission", plugin));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label
                    + " <base_colour> [pattern:colour ...] [player]");
            return true;
        }

        // Parse base colour
        DyeColor baseColor = parseDye(args[0]);
        if (baseColor == null) {
            sender.sendMessage(Messages.get("banner-invalid-color", plugin, "value", args[0]));
            return true;
        }

        int maxLayers = plugin.getConfig().getInt("max-banner-layers", 6);
        List<Pattern> layers = new ArrayList<>();
        Player target = null;

        // Parse remaining args: pattern:colour OR player name
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];

            // Check if this arg is a pattern:colour pair
            if (arg.contains(":")) {
                String[] parts = arg.split(":", 2);
                PatternType patternType = parsePattern(parts[0]);
                if (patternType == null) {
                    sender.sendMessage(Messages.get("banner-invalid-pattern", plugin,
                            "value", parts[0]));
                    return true;
                }
                DyeColor patColor = parseDye(parts[1]);
                if (patColor == null) {
                    sender.sendMessage(Messages.get("banner-invalid-color", plugin,
                            "value", parts[1]));
                    return true;
                }
                if (layers.size() >= maxLayers) {
                    sender.sendMessage(Messages.get("banner-too-many-layers", plugin,
                            "max", String.valueOf(maxLayers)));
                    return true;
                }
                layers.add(new Pattern(patColor, patternType));
            } else {
                // Treat as optional player name (must be the last argument)
                if (i == args.length - 1) {
                    target = Bukkit.getPlayerExact(arg);
                    if (target == null) {
                        // Could still be a mistyped pattern — give informative error
                        sender.sendMessage(Messages.get("player-not-found", plugin, "player", arg));
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Unknown argument: " + arg
                            + ". Pattern args must be in pattern:colour format.");
                    return true;
                }
            }
        }

        // Default target to sender
        if (target == null) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a target player.");
                return true;
            }
            target = p;
        }

        // Build banner
        ItemStack banner = buildBanner(baseColor, layers);
        if (target.getInventory().firstEmpty() == -1) {
            target.getWorld().dropItemNaturally(target.getLocation(), banner);
            target.sendMessage(Messages.get("inventory-full", plugin));
        } else {
            target.getInventory().addItem(banner);
            target.sendMessage(Messages.get("banner-created", plugin));
        }

        if (!target.equals(sender)) {
            sender.sendMessage(ChatColor.GREEN + "Created banner for "
                    + ChatColor.YELLOW + target.getName() + ChatColor.GREEN + ".");
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Tab completion
    // ------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 1) {
            // Base colour
            return dyeColorNames(args[0]);
        }
        if (args.length >= 2) {
            String current = args[args.length - 1];
            // If it already contains ':', complete the colour portion
            if (current.contains(":")) {
                String colorPart = current.substring(current.indexOf(':') + 1);
                String patPart = current.substring(0, current.indexOf(':') + 1);
                return dyeColorNames(colorPart).stream()
                        .map(c -> patPart + c)
                        .collect(Collectors.toList());
            }
            // Otherwise suggest patterns, player names, or colours
            List<String> suggestions = new ArrayList<>();
            String lower = current.toLowerCase(Locale.ROOT);

            // Pattern names (will be appended with ':')
            for (PatternType pt : EnchantedLoomGUI.getAllPatterns()) {
                String key = pt.toString().toLowerCase(Locale.ROOT);
                if (key.startsWith(lower)) suggestions.add(key + ":");
            }
            // Online player names (last arg can be a player)
            Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(lower))
                    .forEach(suggestions::add);

            return suggestions;
        }
        return List.of();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private ItemStack buildBanner(DyeColor base, List<Pattern> layers) {
        ItemStack banner = new ItemStack(EnchantedLoomGUI.bannerMaterialFor(base));
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        if (meta != null) {
            for (Pattern layer : layers) {
                meta.addPattern(layer);
            }
            banner.setItemMeta(meta);
        }
        return banner;
    }

    private DyeColor parseDye(String name) {
        try {
            return DyeColor.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private PatternType parsePattern(String name) {
        // Try direct name match first
        try {
            return PatternType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            // fall through
        }
        // Try registry key lookup
        for (PatternType pt : EnchantedLoomGUI.getAllPatterns()) {
            if (pt.toString().equalsIgnoreCase(name)) return pt;
            var key = Registry.BANNER_PATTERN.getKey(pt);
            if (key != null && key.getKey().equalsIgnoreCase(name)) return pt;
        }
        return null;
    }

    private List<String> dyeColorNames(String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return Arrays.stream(DyeColor.values())
                .map(d -> d.name().toLowerCase(Locale.ROOT))
                .filter(n -> n.startsWith(lower))
                .collect(Collectors.toList());
    }
}
