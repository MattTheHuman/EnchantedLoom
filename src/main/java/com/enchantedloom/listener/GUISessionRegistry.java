package com.enchantedloom.listener;

import com.enchantedloom.gui.EnchantedLoomGUI;
import com.enchantedloom.gui.GUISession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Thread-local (main-thread only) registry that maps player UUIDs to their
 * active GUI session and GUI instance.
 */
public final class GUISessionRegistry {

    private GUISessionRegistry() {}

    private record Entry(GUISession session, EnchantedLoomGUI gui) {}

    private static final Map<UUID, Entry> REGISTRY = new HashMap<>();

    public static void register(UUID uuid, GUISession session, EnchantedLoomGUI gui) {
        REGISTRY.put(uuid, new Entry(session, gui));
    }

    public static GUISession getSession(UUID uuid) {
        Entry e = REGISTRY.get(uuid);
        return e == null ? null : e.session();
    }

    public static EnchantedLoomGUI getGUI(UUID uuid) {
        Entry e = REGISTRY.get(uuid);
        return e == null ? null : e.gui();
    }

    public static void remove(UUID uuid) {
        REGISTRY.remove(uuid);
    }

    public static boolean hasSession(UUID uuid) {
        return REGISTRY.containsKey(uuid);
    }
}
