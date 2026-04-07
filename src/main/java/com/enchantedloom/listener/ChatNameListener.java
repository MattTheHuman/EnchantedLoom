package com.enchantedloom.listener;

import com.enchantedloom.EnchantedLoomPlugin;
import com.enchantedloom.gui.EnchantedLoomGUI;
import com.enchantedloom.gui.GUISession;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Intercepts the next chat message from a specific player to capture a
 * banner design name when the player uses the "Save Design" button.
 */
public class ChatNameListener implements Listener {

    private final EnchantedLoomPlugin plugin;
    /** Maps player UUID → their pending save request. */
    private final Map<UUID, PendingSave> pending = new HashMap<>();

    public ChatNameListener(EnchantedLoomPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register a player as awaiting name input.
     * The listener will capture their next chat message and save the banner.
     */
    public void awaitName(UUID uuid, GUISession session, EnchantedLoomGUI gui) {
        pending.put(uuid, new PendingSave(session, gui));
    }

    public boolean isAwaiting(UUID uuid) {
        return pending.containsKey(uuid);
    }

    /** Cancel any pending name-await for a player (e.g. GUI closed manually). */
    public void cancelAwait(UUID uuid) {
        pending.remove(uuid);
    }

    // -------------------------------------------------------------------------
    // Event handler
    // -------------------------------------------------------------------------

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncChatEvent event) {
        UUID uid = event.getPlayer().getUniqueId();
        if (!pending.containsKey(uid)) return;

        // Cancel so the message is not broadcast to other players
        event.setCancelled(true);

        // input is the raw MiniMessage string the player typed (e.g. "<red>My Banner</red>")
        String input = PlainTextComponentSerializer.plainText()
                .serialize(event.message()).trim();
        PendingSave ps = pending.remove(uid);
        Player player = event.getPlayer();

        // Derive the plain-text key from the raw input (strips any MiniMessage tags)
        String plainName = PlainTextComponentSerializer.plainText()
                .serialize(MiniMessage.miniMessage().deserialize(input));

        // All inventory/session operations must run on the main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (input.equalsIgnoreCase("cancel") || input.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "Banner save cancelled.");
            } else if (plainName.isBlank()) {
                player.sendMessage(ChatColor.RED + "Name cannot be empty. Save cancelled.");
            } else if (plainName.length() > 32) {
                player.sendMessage(ChatColor.RED + "Name is too long (max 32 characters). Save cancelled.");
            } else {
                plugin.getBannerStorage().saveBanner(
                        uid,
                        plainName,
                        input,       // MiniMessage display string
                        ps.session().getBaseColor(),
                        ps.session().getLayers()
                );
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<green>Banner saved as '</green>" + input + "<green>'!</green>"));
            }

            // Reopen the GUI in the PATTERN step so the player can continue designing
            ps.session().setStep(GUISession.Step.PATTERN);
            ps.session().setTransitioning(false);
            ps.gui().open();
            GUISessionRegistry.register(uid, ps.session(), ps.gui());
        });
    }

    // -------------------------------------------------------------------------

    private record PendingSave(GUISession session, EnchantedLoomGUI gui) {}
}
