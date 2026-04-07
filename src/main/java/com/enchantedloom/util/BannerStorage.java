package com.enchantedloom.util;

import com.enchantedloom.EnchantedLoomPlugin;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Persists banner designs to {@code plugins/EnchantedLoom/banners.yml}.
 *
 * <p>Storage format per layer: {@code namespace:key|DYE_COLOR_NAME}
 * e.g. {@code minecraft:stripe_top|BLACK}
 */
public class BannerStorage {

    private final EnchantedLoomPlugin plugin;
    private final File file;
    private YamlConfiguration yaml;

    public BannerStorage(EnchantedLoomPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "banners.yml");
        reload();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Save (or overwrite) a named banner design for a player.
     * Names are trimmed and capped at 32 characters before being stored.
     */
    public void saveBanner(UUID playerId, String name, String displayName, DyeColor base, List<Pattern> layers) {
        String safeName = name.trim().replace(".", "-");
        String path = playerPath(playerId) + "." + safeName;
        yaml.set(path + ".base", base.name());
        yaml.set(path + ".display", displayName);

        List<String> encoded = new ArrayList<>();
        for (Pattern p : layers) {
            NamespacedKey key = Registry.BANNER_PATTERN.getKey(p.getPattern());
            if (key == null) continue; // skip unregistered patterns
            encoded.add(key + "|" + p.getColor().name());
        }
        yaml.set(path + ".layers", encoded);
        flush();
    }

    /**
     * Returns all saved banners for a player, ordered by insertion.
     */
    public Map<String, SavedBanner> getBanners(UUID playerId) {
        Map<String, SavedBanner> result = new LinkedHashMap<>();
        ConfigurationSection player = yaml.getConfigurationSection(playerPath(playerId));
        if (player == null) return result;

        for (String name : player.getKeys(false)) {
            String baseName = player.getString(name + ".base", "WHITE");
            List<String> encoded = player.getStringList(name + ".layers");

            DyeColor base;
            try { base = DyeColor.valueOf(baseName); }
            catch (IllegalArgumentException e) { base = DyeColor.WHITE; }

            List<Pattern> layers = new ArrayList<>();
            for (String entry : encoded) {
                int pipe = entry.lastIndexOf('|');
                if (pipe < 0) continue;
                String keyStr   = entry.substring(0, pipe);
                String colorStr = entry.substring(pipe + 1);

                NamespacedKey nsk = NamespacedKey.fromString(keyStr);
                if (nsk == null) continue;
                PatternType type = Registry.BANNER_PATTERN.get(nsk);
                if (type == null) continue;

                try {
                    DyeColor color = DyeColor.valueOf(colorStr);
                    layers.add(new Pattern(color, type));
                } catch (IllegalArgumentException ignored) {}
            }
            String display = player.getString(name + ".display", name);
            result.put(name, new SavedBanner(name, display, base, layers, playerId));
        }
        return result;
    }

    /**
     * Returns all saved banners across every player, ordered by player then insertion.
     */
    public List<SavedBanner> getAllBanners() {
        List<SavedBanner> result = new ArrayList<>();
        ConfigurationSection players = yaml.getConfigurationSection("players");
        if (players == null) return result;
        for (String uuidStr : players.getKeys(false)) {
            try {
                UUID id = UUID.fromString(uuidStr);
                result.addAll(getBanners(id).values());
            } catch (IllegalArgumentException ignored) {}
        }
        return result;
    }

    /**
     * Delete a saved banner by name. No-op if it does not exist.
     */
    public void deleteBanner(UUID playerId, String name) {
        String playerSection = playerPath(playerId);
        yaml.set(playerSection + "." + name, null);
        ConfigurationSection sec = yaml.getConfigurationSection(playerSection);
        if (sec != null && sec.getKeys(false).isEmpty()) {
            yaml.set(playerSection, null);
        }
        flush();
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private String playerPath(UUID id) {
        return "players." + id;
    }

    private void reload() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try { file.createNewFile(); }
            catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not create banners.yml", e);
            }
        }
        yaml = YamlConfiguration.loadConfiguration(file);
    }

    private void flush() {
        try { yaml.save(file); }
        catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save banners.yml", e);
        }
    }

    // -------------------------------------------------------------------------
    // Value type
    // -------------------------------------------------------------------------

    public record SavedBanner(String name, String displayName, DyeColor base, List<Pattern> layers, UUID ownerId) {}
}
