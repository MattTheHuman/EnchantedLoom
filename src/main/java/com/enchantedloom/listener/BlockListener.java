package com.enchantedloom.listener;

import com.enchantedloom.EnchantedLoomPlugin;
import com.enchantedloom.gui.EnchantedLoomGUI;
import com.enchantedloom.gui.GUISession;
import com.enchantedloom.util.Messages;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Intercepts interactions with an Enchanted Loom block/item.
 * <p>
 * An Enchanted Loom is identified via its PDC marker. When placed as a
 * block the PDC is on the loom item held; the placed block itself is a
 * vanilla loom – player interaction with it is caught here to open the GUI.
 */
public class BlockListener implements Listener {

    private final EnchantedLoomPlugin plugin;
    /** Tracks which loom block locations were originally placed by an Enchanted Loom item. */
    private final Map<UUID, Long> recentLoomInteractions = new HashMap<>();

    public BlockListener(EnchantedLoomPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only care about right-click
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        // Ignore off-hand to avoid double-firing
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();

        // --- Case 1: right-clicking a loom block ---
        Block clicked = event.getClickedBlock();
        if (clicked != null && clicked.getType() == Material.LOOM) {
            if (!player.hasPermission("enchantedloom.use")) {
                player.sendMessage(Messages.get("no-permission", plugin));
                event.setCancelled(true);
                return;
            }
            // Cancel the vanilla loom UI and open our GUI instead
            event.setCancelled(true);
            openGUI(player);
            return;
        }

        // --- Case 2: right-clicking with the Enchanted Loom item in hand ---
        ItemStack hand = event.getItem();
        if (hand != null && plugin.getItemFactory().isEnchantedLoom(hand)) {
            if (!player.hasPermission("enchantedloom.use")) {
                player.sendMessage(Messages.get("no-permission", plugin));
                event.setCancelled(true);
                return;
            }
            // If they clicked air or a non-loom block, open GUI directly
            if (clicked == null || clicked.getType() != Material.LOOM) {
                event.setCancelled(true);
                openGUI(player);
            }
            // If they clicked a loom block, the Case 1 handler above fires first
        }
    }

    private void openGUI(Player player) {
        GUISession session = new GUISession(player);
        EnchantedLoomGUI gui = new EnchantedLoomGUI(plugin, session);
        gui.open();
        // Store session so GUIListener can retrieve it
        GUISessionRegistry.register(player.getUniqueId(), session, gui);
    }
}
