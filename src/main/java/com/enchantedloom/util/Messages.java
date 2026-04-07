package com.enchantedloom.util;

import org.bukkit.ChatColor;

/**
 * Lightweight helper around config message lookups.
 */
public final class Messages {

    private Messages() {}

    /**
     * Resolves a message path from config, applies colour codes and
     * replaces any %placeholder% tokens supplied as key/value pairs.
     *
     * @param path   config path under "messages."
     * @param plugin plugin instance
     * @param args   alternating placeholder-name / replacement pairs
     * @return formatted message string
     */
    public static String get(String path,
                             com.enchantedloom.EnchantedLoomPlugin plugin,
                             String... args) {
        // Fall back to the default config value so missing keys in a player's
        // server config never surface as a raw "Message not found" string.
        String defaultValue = plugin.getConfig().getDefaults() != null
                ? plugin.getConfig().getDefaults().getString("messages." + path, "&cMessage not found: " + path)
                : "&cMessage not found: " + path;
        String raw = plugin.getConfig().getString("messages." + path, defaultValue);
        // Replace placeholders
        for (int i = 0; i + 1 < args.length; i += 2) {
            raw = raw.replace("%" + args[i] + "%", args[i + 1]);
        }
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
