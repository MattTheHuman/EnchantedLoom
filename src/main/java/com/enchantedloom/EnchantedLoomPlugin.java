package com.enchantedloom;

import com.enchantedloom.command.BannerCommand;
import com.enchantedloom.command.EnchantedLoomCommand;
import com.enchantedloom.command.GiveCommand;
import com.enchantedloom.command.OpenCommand;
import com.enchantedloom.command.ReloadCommand;
import com.enchantedloom.listener.BlockListener;
import com.enchantedloom.listener.ChatNameListener;
import com.enchantedloom.listener.GUIListener;
import com.enchantedloom.util.BannerStorage;
import com.enchantedloom.util.EnchantedLoomRegistry;
import com.enchantedloom.util.ItemFactory;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Main entry-point for the EnchantedLoom plugin.
 */
public class EnchantedLoomPlugin extends JavaPlugin {

    private static EnchantedLoomPlugin instance;
    private ItemFactory itemFactory;
    private BannerStorage bannerStorage;
    private ChatNameListener chatNameListener;
    private EnchantedLoomRegistry loomRegistry;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config, then copy any new keys added since the server's copy was created
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Build shared item factory
        itemFactory = new ItemFactory(this);
        bannerStorage = new BannerStorage(this);
        chatNameListener = new ChatNameListener(this);
        loomRegistry = new EnchantedLoomRegistry(this);

        // Register crafting recipes
        registerRecipes();

        // Register listeners
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(chatNameListener, this);

        // Register commands
        Objects.requireNonNull(getCommand("enchantedloom")).setExecutor(new EnchantedLoomCommand(this));
        Objects.requireNonNull(getCommand("enchantedloom")).setTabCompleter(new EnchantedLoomCommand(this));
        Objects.requireNonNull(getCommand("elopen")).setExecutor(new OpenCommand(this));
        Objects.requireNonNull(getCommand("elopen")).setTabCompleter(new OpenCommand(this));
        Objects.requireNonNull(getCommand("elgive")).setExecutor(new GiveCommand(this));
        Objects.requireNonNull(getCommand("elgive")).setTabCompleter(new GiveCommand(this));
        Objects.requireNonNull(getCommand("elbanner")).setExecutor(new BannerCommand(this));
        Objects.requireNonNull(getCommand("elbanner")).setTabCompleter(new BannerCommand(this));
        ReloadCommand reloadCmd = new ReloadCommand(this);
        Objects.requireNonNull(getCommand("elreload")).setExecutor(reloadCmd);

        getLogger().info("EnchantedLoom enabled successfully!");
    }

    @Override
    public void onDisable() {
        loomRegistry.save();
        getLogger().info("EnchantedLoom disabled.");
    }

    /**
     * Reloads the plugin configuration from disk, writing any missing defaults first.
     */
    public void reloadPlugin() {
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void registerRecipes() {
        // --- Diamond String: 4 diamonds in a cross around 1 string ---
        ItemStack diamondString = itemFactory.createDiamondString(1);
        ShapedRecipe dsRecipe = new ShapedRecipe(
                new NamespacedKey(this, "diamond_string"), diamondString);
        dsRecipe.shape(" D ", "DSD", " D ");
        dsRecipe.setIngredient('D', Material.DIAMOND);
        dsRecipe.setIngredient('S', Material.STRING);
        getServer().addRecipe(dsRecipe);

        // --- Enchanted Loom: 2 Diamond String + 2 planks (mirrors the vanilla loom layout) ---
        ItemStack enchantedLoom = itemFactory.createEnchantedLoom(1);
        ShapedRecipe elRecipe = new ShapedRecipe(
                new NamespacedKey(this, "enchanted_loom"), enchantedLoom);
        elRecipe.shape("AA", "BB");
        elRecipe.setIngredient('A', new RecipeChoice.ExactChoice(diamondString));
        elRecipe.setIngredient('B', new RecipeChoice.MaterialChoice(Tag.PLANKS));
        getServer().addRecipe(elRecipe);
    }

    public static EnchantedLoomPlugin getInstance() {
        return instance;
    }

    public ItemFactory getItemFactory() {
        return itemFactory;
    }

    public BannerStorage getBannerStorage() {
        return bannerStorage;
    }

    public ChatNameListener getChatNameListener() {
        return chatNameListener;
    }

    public EnchantedLoomRegistry getLoomRegistry() {
        return loomRegistry;
    }
}
