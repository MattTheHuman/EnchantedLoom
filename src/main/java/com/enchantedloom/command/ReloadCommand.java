package com.enchantedloom.command;

import com.enchantedloom.EnchantedLoomPlugin;
import com.enchantedloom.util.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /elreload — reloads the EnchantedLoom configuration from disk.
 * Requires the enchantedloom.admin permission.
 */
public class ReloadCommand implements CommandExecutor {

    private final EnchantedLoomPlugin plugin;

    public ReloadCommand(EnchantedLoomPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("enchantedloom.admin")) {
            sender.sendMessage(Messages.get("no-permission", plugin));
            return true;
        }
        plugin.reloadPlugin();
        sender.sendMessage(org.bukkit.ChatColor.GREEN + "EnchantedLoom configuration reloaded.");
        return true;
    }
}
