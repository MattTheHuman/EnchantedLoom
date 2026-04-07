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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates and identifies the Enchanted Loom and Diamond String ItemStacks.
 */
public class ItemFactory {

    /** PDC key used to mark an item as an Enchanted Loom. */
    public static final String PDC_KEY = "enchanted_loom";
    /** PDC key used to mark an item as Diamond String. */
    public static final String DIAMOND_STRING_KEY = "diamond_string";

    private final EnchantedLoomPlugin plugin;
    private final NamespacedKey loomKey;
    private final NamespacedKey diamondStringKey;

    public ItemFactory(EnchantedLoomPlugin plugin) {
        this.plugin = plugin;
        this.loomKey = new NamespacedKey(plugin, PDC_KEY);
        this.diamondStringKey = new NamespacedKey(plugin, DIAMOND_STRING_KEY);
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

    /**
     * Builds a Diamond String ItemStack — the crafting ingredient for the Enchanted Loom.
     *
     * @param amount number of items
     * @return the configured ItemStack
     */
    public ItemStack createDiamondString(int amount) {
        ItemStack item = new ItemStack(Material.STRING, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Diamond String");
        meta.setLore(List.of(ChatColor.GRAY + "Infused with diamond dust"));
        meta.getPersistentDataContainer().set(diamondStringKey, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Returns true if the given ItemStack is a Diamond String.
     */
    public boolean isDiamondString(ItemStack item) {
        if (item == null || item.getType() != Material.STRING) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(diamondStringKey, PersistentDataType.BYTE);
    }

    public NamespacedKey getLoomKey() {
        return loomKey;
    }

    public NamespacedKey getDiamondStringKey() {
        return diamondStringKey;
    }
}
