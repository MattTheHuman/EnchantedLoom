package com.enchantedloom.command;

import com.enchantedloom.EnchantedLoomPlugin;
import com.enchantedloom.gui.EnchantedLoomGUI;
import com.enchantedloom.gui.GUISession;
import com.enchantedloom.listener.GUISessionRegistry;
import com.enchantedloom.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /elopen [player] — opens the Enchanted Loom GUI for the sender or a target.
 */
public class OpenCommand implements CommandExecutor, TabCompleter {

    private final EnchantedLoomPlugin plugin;

    public OpenCommand(EnchantedLoomPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;

        if (args.length >= 1) {
            // Admin opening for another player
            if (!sender.hasPermission("enchantedloom.admin")) {
                sender.sendMessage(Messages.get("no-permission", plugin));
                return true;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(Messages.get("player-not-found", plugin, "player", args[0]));
                return true;
            }
        } else {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(org.bukkit.ChatColor.RED
                        + "Console must specify a target player: /elopen <player>");
                return true;
            }
            target = p;
        }

        if (!target.hasPermission("enchantedloom.open")) {
            sender.sendMessage(Messages.get("no-permission", plugin));
            return true;
        }

        GUISession session = new GUISession(target);
        EnchantedLoomGUI gui = new EnchantedLoomGUI(plugin, session);
        gui.open();
        GUISessionRegistry.register(target.getUniqueId(), session, gui);

        if (!target.equals(sender)) {
            sender.sendMessage(Messages.get("gui-opened", plugin));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("enchantedloom.admin")) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
