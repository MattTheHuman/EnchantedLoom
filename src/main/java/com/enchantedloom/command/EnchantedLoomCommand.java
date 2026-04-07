package com.enchantedloom.command;

import com.enchantedloom.EnchantedLoomPlugin;
import com.enchantedloom.util.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

/**
 * Root /enchantedloom (alias /eloom) command that delegates to sub-commands.
 */
public class EnchantedLoomCommand implements CommandExecutor, TabCompleter {

    private final EnchantedLoomPlugin plugin;
    private final OpenCommand openCmd;
    private final GiveCommand giveCmd;
    private final BannerCommand bannerCmd;

    public EnchantedLoomCommand(EnchantedLoomPlugin plugin) {
        this.plugin    = plugin;
        this.openCmd   = new OpenCommand(plugin);
        this.giveCmd   = new GiveCommand(plugin);
        this.bannerCmd = new BannerCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        String[] subArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
        return switch (sub) {
            case "open"   -> openCmd.onCommand(sender, command, label, subArgs);
            case "give"   -> giveCmd.onCommand(sender, command, label, subArgs);
            case "banner" -> bannerCmd.onCommand(sender, command, label, subArgs);
            case "reload" -> handleReload(sender);
            case "help"   -> { sendHelp(sender); yield true; }
            default       -> { sendHelp(sender); yield true; }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 1) {
            return List.of("open", "give", "banner", "reload", "help");
        }
        if (args.length >= 2) {
            String sub = args[0].toLowerCase();
            String[] subArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
            return switch (sub) {
                case "open"   -> openCmd.onTabComplete(sender, command, alias, subArgs);
                case "give"   -> giveCmd.onTabComplete(sender, command, alias, subArgs);
                case "banner" -> bannerCmd.onTabComplete(sender, command, alias, subArgs);
                default       -> List.of();
            };
        }
        return List.of();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(org.bukkit.ChatColor.LIGHT_PURPLE + "--- EnchantedLoom Commands ---");
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "/eloom open [player]"
                + org.bukkit.ChatColor.GRAY + " - Open the Enchanted Loom GUI");
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "/eloom give [player] [amount]"
                + org.bukkit.ChatColor.GRAY + " - Give an Enchanted Loom item");
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "/eloom banner <base> [pattern:colour ...] [player]"
                + org.bukkit.ChatColor.GRAY + " - Generate a patterned banner");
        sender.sendMessage(org.bukkit.ChatColor.YELLOW + "/eloom reload"
                + org.bukkit.ChatColor.GRAY + " - Reload the plugin configuration");
        sender.sendMessage(org.bukkit.ChatColor.GRAY
                + "Shorthand commands: /elopen, /elgive, /elbanner, /elreload");
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("enchantedloom.admin")) {
            sender.sendMessage(Messages.get("no-permission", plugin));
            return true;
        }
        plugin.reloadPlugin();
        sender.sendMessage(org.bukkit.ChatColor.GREEN + "EnchantedLoom configuration reloaded.");
        return true;
    }
}
