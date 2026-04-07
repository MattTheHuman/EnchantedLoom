package com.enchantedloom;

import com.enchantedloom.command.BannerCommand;
import com.enchantedloom.command.EnchantedLoomCommand;
import com.enchantedloom.command.GiveCommand;
import com.enchantedloom.command.OpenCommand;
import com.enchantedloom.listener.BlockListener;
import com.enchantedloom.listener.ChatNameListener;
import com.enchantedloom.listener.GUIListener;
import com.enchantedloom.util.BannerStorage;
import com.enchantedloom.util.ItemFactory;
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

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Build shared item factory
        itemFactory = new ItemFactory(this);
        bannerStorage = new BannerStorage(this);
        chatNameListener = new ChatNameListener(this);

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

        getLogger().info("EnchantedLoom enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("EnchantedLoom disabled.");
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
}
