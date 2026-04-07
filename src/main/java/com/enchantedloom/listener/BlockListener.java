package com.enchantedloom.listener;

import com.enchantedloom.EnchantedLoomPlugin;
import com.enchantedloom.gui.EnchantedLoomGUI;
import com.enchantedloom.gui.GUISession;
import com.enchantedloom.util.Messages;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

/**
 * Manages the lifecycle of placed Enchanted Loom blocks and intercepts right-click
 * interactions on those blocks to open the custom GUI.
 * <p>
 * Vanilla loom blocks are never intercepted — only blocks registered in
 * {@link com.enchantedloom.util.EnchantedLoomRegistry} open the custom GUI.
 */
public class BlockListener implements Listener {

    private final EnchantedLoomPlugin plugin;

    public BlockListener(EnchantedLoomPlugin plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Block lifecycle — keep registry in sync
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getItemFactory().isEnchantedLoom(event.getItemInHand())) {
            plugin.getLoomRegistry().add(event.getBlockPlaced().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.LOOM) {
            plugin.getLoomRegistry().remove(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().stream()
                .filter(b -> b.getType() == Material.LOOM)
                .forEach(b -> plugin.getLoomRegistry().remove(b.getLocation()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().stream()
                .filter(b -> b.getType() == Material.LOOM)
                .forEach(b -> plugin.getLoomRegistry().remove(b.getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        relocateBlocks(event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        relocateBlocks(event.getBlocks(), event.getDirection());
    }

    private void relocateBlocks(List<Block> blocks, BlockFace direction) {
        for (Block block : blocks) {
            if (block.getType() == Material.LOOM
                    && plugin.getLoomRegistry().contains(block.getLocation())) {
                plugin.getLoomRegistry().remove(block.getLocation());
                plugin.getLoomRegistry().add(block.getRelative(direction).getLocation());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Interaction
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.LOOM) return;

        // Only intercept blocks registered as Enchanted Looms; leave vanilla looms alone
        if (!plugin.getLoomRegistry().contains(clicked.getLocation())) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("enchantedloom.use")) {
            player.sendMessage(Messages.get("no-permission", plugin));
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        openGUI(player);
    }

    private void openGUI(Player player) {
        GUISession session = new GUISession(player);
        EnchantedLoomGUI gui = new EnchantedLoomGUI(plugin, session);
        gui.open();
        GUISessionRegistry.register(player.getUniqueId(), session, gui);
    }
}
