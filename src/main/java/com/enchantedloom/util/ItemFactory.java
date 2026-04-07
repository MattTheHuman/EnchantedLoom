package com.enchantedloom.util;

import com.enchantedloom.EnchantedLoomPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates and identifies the Enchanted Loom ItemStack.
 */
public class ItemFactory {

    /** PDC key used to mark an item as an Enchanted Loom. */
    public static final String PDC_KEY = "enchanted_loom";

    private final EnchantedLoomPlugin plugin;
    private final NamespacedKey loomKey;

    public ItemFactory(EnchantedLoomPlugin plugin) {
        this.plugin = plugin;
        this.loomKey = new NamespacedKey(plugin, PDC_KEY);
    }

    /**
     * Builds an Enchanted Loom ItemStack according to config values.
     *
     * @param amount number of items
     * @return the configured ItemStack
     */
    public ItemStack createEnchantedLoom(int amount) {
        ItemStack item = new ItemStack(Material.LOOM, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String rawName = plugin.getConfig().getString("loom-name", "&5&lEnchanted Loom");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', rawName));

        List<String> rawLore = plugin.getConfig().getStringList("loom-lore");
        List<String> lore = rawLore.stream()
                .map(l -> ChatColor.translateAlternateColorCodes('&', l))
                .collect(Collectors.toList());
        meta.setLore(lore);

        // Apply a hidden enchantment glint if configured
        if (plugin.getConfig().getBoolean("item-glow", true)) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // PDC marker so we can recognise it later without relying on display name
        meta.getPersistentDataContainer().set(loomKey, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Returns true if the given ItemStack is an Enchanted Loom.
     */
    public boolean isEnchantedLoom(ItemStack item) {
        if (item == null || item.getType() != Material.LOOM) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(loomKey, PersistentDataType.BYTE);
    }

    public NamespacedKey getLoomKey() {
        return loomKey;
    }
}
