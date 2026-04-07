package com.enchantedloom.command;

import com.enchantedloom.EnchantedLoomPlugin;
import com.enchantedloom.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /elgive [player] [amount] — gives an Enchanted Loom item.
 */
public class GiveCommand implements CommandExecutor, TabCompleter {

    private final EnchantedLoomPlugin plugin;

    public GiveCommand(EnchantedLoomPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("enchantedloom.give")) {
            sender.sendMessage(Messages.get("no-permission", plugin));
            return true;
        }

        Player target;
        int amount = 1;

        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player: /elgive <player> [amount]");
                return true;
            }
            target = p;
        } else {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(Messages.get("player-not-found", plugin, "player", args[0]));
                return true;
            }
        }

        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 1) amount = 1;
                if (amount > 64) amount = 64;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount. Must be 1–64.");
                return true;
            }
        }

        ItemStack loom = plugin.getItemFactory().createEnchantedLoom(amount);

        if (target.getInventory().firstEmpty() == -1) {
            target.getWorld().dropItemNaturally(target.getLocation(), loom);
            sender.sendMessage(Messages.get("inventory-full", plugin));
        } else {
            target.getInventory().addItem(loom);
        }

        sender.sendMessage(Messages.get("item-given", plugin,
                "player", target.getName(),
                "amount", String.valueOf(amount)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
                                      String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("enchantedloom.give")) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return List.of("1", "4", "8", "16", "32", "64");
        }
        return List.of();
    }
}
