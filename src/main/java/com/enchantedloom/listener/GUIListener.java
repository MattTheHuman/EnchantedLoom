package com.enchantedloom.listener;

import com.enchantedloom.EnchantedLoomPlugin;
import com.enchantedloom.gui.EnchantedLoomGUI;
import com.enchantedloom.gui.GUISession;
import com.enchantedloom.util.BannerStorage;
import com.enchantedloom.util.Messages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles all player interactions inside the Enchanted Loom wizard GUI.
 *
 * Step routing:
 *   BASE_COLOR     -- click a colour swatch -> set base colour, go to PATTERN
 *   PATTERN        -- click pattern item    -> set pending, go to PATTERN_COLOR
 *                     click Prev/Next       -> paginate (redraw)
 *                     click Undo            -> remove last layer (redraw)
 *                     click Saved Banners   -> go to SAVED_BANNERS step
 *                     click Save Design     -> close GUI, await chat name
 *                     click Take Banner     -- give banner (GUI stays open)
 *   PATTERN_COLOR  -- click a colour swatch -> add layer, go to PATTERN
 *                     click Back (slot 45)  -> go to PATTERN, no layer added
 *   SAVED_BANNERS  -- left-click a banner   -> give a copy (GUI stays open)
 *                     shift-click a banner  -> delete saved design (redraw)
 *                     click Prev/Next       -> paginate (redraw)
 *                     click Back (slot 53)  -> go to PATTERN step
 */
public class GUIListener implements Listener {

    private final EnchantedLoomPlugin plugin;

    public GUIListener(EnchantedLoomPlugin plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Cancel drags inside the GUI
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!GUISessionRegistry.hasSession(player.getUniqueId())) return;
        event.setCancelled(true);
    }

    // -------------------------------------------------------------------------
    // Click routing
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID uid = player.getUniqueId();
        if (!GUISessionRegistry.hasSession(uid)) return;

        // Always cancel -- players never take items from the GUI
        event.setCancelled(true);

        // Only process clicks inside the top (GUI) inventory
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        GUISession session = GUISessionRegistry.getSession(uid);
        EnchantedLoomGUI gui = GUISessionRegistry.getGUI(uid);
        if (session == null || gui == null) return;

        int slot = event.getSlot();
        boolean isShift = event.isShiftClick();
        switch (session.getStep()) {
            case BASE_COLOR    -> handleBaseColor(slot, session, gui, player);
            case PATTERN       -> handlePattern(slot, session, gui, player);
            case PATTERN_COLOR -> handlePatternColor(slot, session, gui, player);
            case SAVED_BANNERS -> handleSavedBanners(slot, isShift, session, gui, player);
        }
    }

    // -------------------------------------------------------------------------
    // Step: BASE_COLOR
    // -------------------------------------------------------------------------

    private void handleBaseColor(int slot, GUISession session, EnchantedLoomGUI gui, Player player) {
        if (slot == EnchantedLoomGUI.getSlotBcSaved()) {
            session.setSavedBannersPage(0);
            gui.transition(GUISession.Step.SAVED_BANNERS);
            return;
        }

        int idx = colorIndex(slot);
        if (idx < 0) return;
        List<DyeColor> colors = EnchantedLoomGUI.getDyeColors();
        if (idx >= colors.size()) return;

        session.setBaseColor(colors.get(idx));
        gui.transition(GUISession.Step.PATTERN);
    }

    // -------------------------------------------------------------------------
    // Step: PATTERN
    // -------------------------------------------------------------------------

    private void handlePattern(int slot, GUISession session, EnchantedLoomGUI gui, Player player) {
        int maxLayers = plugin.getConfig().getInt("max-banner-layers", 6);
        List<PatternType> patterns = EnchantedLoomGUI.getAllPatterns();

        // Click on a pattern item in the grid (slots 0-44)
        if (slot >= EnchantedLoomGUI.getPatternGridStart()
                && slot < EnchantedLoomGUI.getPatternGridStart() + EnchantedLoomGUI.getPatternGridSize()) {
            if (slot >= patterns.size()) return;  // spare slot (glass)

            if (session.getLayers().size() >= maxLayers) {
                player.sendMessage(Messages.get("banner-too-many-layers", plugin,
                        "max", String.valueOf(maxLayers)));
                return;
            }
            session.setPendingPattern(patterns.get(slot));
            gui.transition(GUISession.Step.PATTERN_COLOR);
            return;
        }

        // Back to Base Colour
        if (slot == EnchantedLoomGUI.getSlotPtBack()) {
            gui.transition(GUISession.Step.BASE_COLOR);
            return;
        }

        // Undo last layer
        if (slot == EnchantedLoomGUI.getSlotPtUndo()) {
            if (!session.getLayers().isEmpty()) {
                session.removeLastLayer();
                gui.redraw();
            }
            return;
        }

        // Open saved banners browser
        if (slot == EnchantedLoomGUI.getSlotPtSaved()) {
            session.setSavedBannersPage(0);
            gui.transition(GUISession.Step.SAVED_BANNERS);
            return;
        }

        // Save design -- close GUI, ask for name in chat
        if (slot == EnchantedLoomGUI.getSlotPtSave()) {
            startSaveFlow(player, session, gui);
            return;
        }

        // Take banner -- consume a blank banner, give the patterned one, keep GUI open
        if (slot == EnchantedLoomGUI.getSlotConfirm()) {
            if (giveBanner(player, session, gui)) {
                session.incrementBannersTaken();
                gui.redraw(); // refresh the "Taken this session" lore
            }
        }
    }

    // -------------------------------------------------------------------------
    // Step: PATTERN_COLOR
    // -------------------------------------------------------------------------

    private void handlePatternColor(int slot, GUISession session, EnchantedLoomGUI gui, Player player) {
        // Back button -- return to PATTERN step without adding a layer
        if (slot == EnchantedLoomGUI.getSlotPcBack()) {
            session.setPendingPattern(null);
            gui.transition(GUISession.Step.PATTERN);
            return;
        }

        int idx = colorIndex(slot);
        if (idx < 0) return;
        List<DyeColor> colors = EnchantedLoomGUI.getDyeColors();
        if (idx >= colors.size()) return;

        PatternType pending = session.getPendingPattern();
        if (pending == null) {
            gui.transition(GUISession.Step.PATTERN);
            return;
        }

        session.addLayer(new Pattern(colors.get(idx), pending));
        session.setPendingPattern(null);
        gui.transition(GUISession.Step.PATTERN);
    }

    // -------------------------------------------------------------------------
    // Step: SAVED_BANNERS
    // -------------------------------------------------------------------------

    private void handleSavedBanners(int slot, boolean isShift, GUISession session,
                                    EnchantedLoomGUI gui, Player player) {
        List<BannerStorage.SavedBanner> banners = gui.getVisibleBanners();

        int page  = session.getSavedBannersPage();
        int start = page * EnchantedLoomGUI.getSavedGridSize();

        // Back button -- return to PATTERN step
        if (slot == EnchantedLoomGUI.getSlotSbBack()) {
            gui.transition(GUISession.Step.PATTERN);
            return;
        }

        // Previous page
        if (slot == EnchantedLoomGUI.getSlotSbPrev() && page > 0) {
            session.setSavedBannersPage(page - 1);
            gui.redraw();
            return;
        }

        // Next page
        if (slot == EnchantedLoomGUI.getSlotSbNext()
                && (page + 1) * EnchantedLoomGUI.getSavedGridSize() < banners.size()) {
            session.setSavedBannersPage(page + 1);
            gui.redraw();
            return;
        }

        // Banner grid click
        if (slot >= 0 && slot < EnchantedLoomGUI.getSavedGridSize()) {
            int idx = start + slot;
            if (idx >= banners.size()) return;
            BannerStorage.SavedBanner saved = banners.get(idx);

            if (isShift) {
                // Only the owner (or an admin) may delete a design
                boolean canDelete = saved.ownerId().equals(player.getUniqueId())
                        || player.hasPermission("enchantedloom.admin");
                if (!canDelete) {
                    player.sendMessage(Messages.get("no-permission", plugin));
                    return;
                }
                plugin.getBannerStorage().deleteBanner(saved.ownerId(), saved.name());
                player.sendMessage(plugin.getConfig().getString(
                        "messages.banner-deleted", "&cDeleted saved banner: &f{name}")
                        .replace("{name}", saved.name())
                        .replace("&", "§"));
                // Adjust page if we deleted the last item on this page
                int remaining = Math.max(0, banners.size() - 1);
                int maxPage = Math.max(0, (int) Math.ceil((double) remaining / EnchantedLoomGUI.getSavedGridSize()) - 1);
                if (session.getSavedBannersPage() > maxPage) {
                    session.setSavedBannersPage(maxPage);
                }
                gui.redraw();
            } else {
                // Give a copy of the saved banner, consuming a blank if required
                giveSavedBanner(player, saved);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Give banner (take action)
    // -------------------------------------------------------------------------

    /**
     * Gives the player a copy of a saved banner design.
     * Respects {@code require-blank-banner} and creative mode in the same way as {@link #giveBanner}.
     */
    private void giveSavedBanner(Player player, BannerStorage.SavedBanner saved) {
        boolean requireBlank = plugin.getConfig().getBoolean("require-blank-banner", true);
        boolean isCreative   = player.getGameMode() == org.bukkit.GameMode.CREATIVE;

        if (requireBlank && !isCreative) {
            Material blankMaterial = EnchantedLoomGUI.bannerMaterialFor(saved.base());
            ItemStack[] contents = player.getInventory().getContents();
            int consumeSlot = -1;
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (item == null || item.getType() != blankMaterial) continue;
                ItemMeta meta = item.getItemMeta();
                if (!(meta instanceof BannerMeta bannerMeta)) continue;
                if (!bannerMeta.getPatterns().isEmpty()) continue;
                consumeSlot = i;
                break;
            }

            if (consumeSlot == -1) {
                player.sendMessage(Messages.get("banner-no-blank-banner", plugin,
                        "color", EnchantedLoomGUI.formatDyeName(saved.base())));
                return;
            }

            ItemStack blank = player.getInventory().getItem(consumeSlot);
            if (blank.getAmount() > 1) {
                blank.setAmount(blank.getAmount() - 1);
            } else {
                player.getInventory().setItem(consumeSlot, null);
            }
        }

        ItemStack banner = buildSavedBannerItem(saved);
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), banner);
            player.sendMessage(Messages.get("inventory-full", plugin));
        } else {
            player.getInventory().addItem(banner);
            player.sendMessage(plugin.getConfig().getString(
                    "messages.banner-retrieved", "&aBanner &f{name}&a retrieved!")
                    .replace("{name}", saved.name())
                    .replace("&", "§"));
        }
    }

    /**
     * Gives the player the patterned banner.
     * If {@code require-blank-banner} is enabled in config and the player is not in creative mode,
     * a matching blank banner is consumed from their inventory first.
     *
     * @return true if the banner was given, false if the player lacked a required blank banner
     */
    private boolean giveBanner(Player player, GUISession session, EnchantedLoomGUI gui) {
        boolean requireBlank = plugin.getConfig().getBoolean("require-blank-banner", true);
        boolean isCreative   = player.getGameMode() == org.bukkit.GameMode.CREATIVE;

        if (requireBlank && !isCreative) {
            Material blankMaterial = EnchantedLoomGUI.bannerMaterialFor(session.getBaseColor());

            // Find a blank (no patterns) banner of the matching colour
            ItemStack[] contents = player.getInventory().getContents();
            int consumeSlot = -1;
            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (item == null || item.getType() != blankMaterial) continue;
                ItemMeta meta = item.getItemMeta();
                if (!(meta instanceof BannerMeta bannerMeta)) continue;
                if (!bannerMeta.getPatterns().isEmpty()) continue;
                consumeSlot = i;
                break;
            }

            if (consumeSlot == -1) {
                player.sendMessage(Messages.get("banner-no-blank-banner", plugin,
                        "color", EnchantedLoomGUI.formatDyeName(session.getBaseColor())));
                return false;
            }

            // Consume the blank banner
            ItemStack blank = player.getInventory().getItem(consumeSlot);
            if (blank.getAmount() > 1) {
                blank.setAmount(blank.getAmount() - 1);
            } else {
                player.getInventory().setItem(consumeSlot, null);
            }
        }

        // Give the patterned banner
        ItemStack banner = gui.buildCurrentBanner();
        if (player.getInventory().firstEmpty() == -1) {
            player.getWorld().dropItemNaturally(player.getLocation(), banner);
            player.sendMessage(Messages.get("inventory-full", plugin));
        } else {
            player.getInventory().addItem(banner);
            player.sendMessage(Messages.get("banner-created", plugin));
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Save flow -- close GUI, start ChatNameListener await
    // -------------------------------------------------------------------------

    private void startSaveFlow(Player player, GUISession session, EnchantedLoomGUI gui) {
        player.sendMessage(plugin.getConfig().getString(
                "messages.save-prompt",
                "&aType a name for this banner design in chat. Type &ccancel&a to abort.")
                .replace("&", "§"));
        // Set transitioning so onInventoryClose does not wipe the session
        session.setTransitioning(true);
        player.closeInventory();
        plugin.getChatNameListener().awaitName(player.getUniqueId(), session, gui);
    }

    // -------------------------------------------------------------------------
    // Build an ItemStack from a SavedBanner (for retrieval from saved list)
    // -------------------------------------------------------------------------

    private ItemStack buildSavedBannerItem(BannerStorage.SavedBanner saved) {
        ItemStack banner = new ItemStack(EnchantedLoomGUI.bannerMaterialFor(saved.base()));
        org.bukkit.inventory.meta.BannerMeta meta =
                (org.bukkit.inventory.meta.BannerMeta) banner.getItemMeta();
        if (meta != null) {
            for (Pattern p : saved.layers()) meta.addPattern(p);
            meta.displayName(MiniMessage.miniMessage().deserialize(saved.displayName()));
            banner.setItemMeta(meta);
        }
        return banner;
    }

    // -------------------------------------------------------------------------
    // Session cleanup on close
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        UUID uid = player.getUniqueId();
        GUISession session = GUISessionRegistry.getSession(uid);
        if (session == null) return;
        // Only clean up if this is a genuine close, not a programmatic step transition
        if (!session.isTransitioning()) {
            plugin.getChatNameListener().cancelAwait(uid);
            GUISessionRegistry.remove(uid);
        }
    }

    // -------------------------------------------------------------------------
    // Helper: map a clicked slot to a DyeColor list index (0-15), or -1
    // -------------------------------------------------------------------------

    private int colorIndex(int slot) {
        int[] slots = EnchantedLoomGUI.getColorSlots();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) return i;
        }
        return -1;
    }
}
