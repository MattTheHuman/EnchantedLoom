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
        String raw = plugin.getConfig().getString("messages." + path,
                "&cMessage not found: " + path);
        // Replace placeholders
        for (int i = 0; i + 1 < args.length; i += 2) {
            raw = raw.replace("%" + args[i] + "%", args[i + 1]);
        }
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
