package com.enchantedloom.util;

import com.enchantedloom.EnchantedLoomPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Tracks which loom blocks in the world were placed from an Enchanted Loom item.
 * Only blocks in this registry open the custom GUI; vanilla looms are left alone.
 * <p>
 * Locations are persisted to {@code plugins/EnchantedLoom/enchanted_looms.yml}
 * so the registry survives server restarts.
 */
public class EnchantedLoomRegistry {

    private final EnchantedLoomPlugin plugin;
    private final File file;
    private final Set<String> locations = new HashSet<>();

    public EnchantedLoomRegistry(EnchantedLoomPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "enchanted_looms.yml");
        load();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public void add(Location loc) {
        locations.add(key(loc));
    }

    public void remove(Location loc) {
        locations.remove(key(loc));
    }

    public boolean contains(Location loc) {
        return locations.contains(key(loc));
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("locations", new ArrayList<>(locations));
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save enchanted_looms.yml", e);
        }
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<String> list = yaml.getStringList("locations");
        locations.addAll(list);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static String key(Location loc) {
        return loc.getWorld().getName()
                + "," + loc.getBlockX()
                + "," + loc.getBlockY()
                + "," + loc.getBlockZ();
    }
}
