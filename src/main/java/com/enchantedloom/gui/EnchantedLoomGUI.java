package com.enchantedloom.gui;

import com.enchantedloom.EnchantedLoomPlugin;
import com.enchantedloom.util.BannerStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.minimessage.MiniMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wizard-style Enchanted Loom GUI -- three active steps.
 *
 * Step 1 BASE_COLOR     -- pick the banner background colour (all 16 visible)
 * Step 2 PATTERN        -- pick a pattern from the paginated grid; Save/Saved buttons here
 * Step 3 PATTERN_COLOR  -- pick the colour for that pattern layer (all 16 visible)
 * Step 4 SAVED_BANNERS  -- browse and retrieve saved banner designs
 *
 * Layout for all steps (54 slots, 6 rows x 9 cols):
 *   Row 0  ( 0- 8): colour/banner grid row 0
 *   Row 1  ( 9-17): colour or banner grid row 1
 *   Row 2  (18-26): colour or banner grid row 2
 *   Row 3  (27-35): (PATTERN) saved-banners | preview | save-design
 *                   (BASE_COLOR/PATTERN_COLOR) banner preview only at slot 31
 *                   (SAVED_BANNERS) banner grid row 3
 *   Row 4  (36-44): glass filler (all steps)
 *   Row 5  (45-53): navigation + action buttons
 */
public class EnchantedLoomGUI {

    // -- Colour-picker slots (BASE_COLOR and PATTERN_COLOR steps) --
    // 16 DyeColor values mapped to 2 rows of 8, centred in rows 1-2
    static final int[] COLOR_SLOTS = {
         9, 10, 11, 12, 13, 14, 15, 16,   // indices 0-7
        18, 19, 20, 21, 22, 23, 24, 25    // indices 8-15
    };

    // -- Pattern step: grid covers rows 0-4 (slots 0-44) --
    static final int PATTERN_GRID_START = 0;
    static final int PATTERN_GRID_SIZE  = 45;

    // -- Row-5 controls (all steps keep content in rows 0-4, controls in row 5) --

    // BASE_COLOR row-5
    static final int SLOT_BC_INFO    = 45;  // step hint         (bottom left)
    static final int SLOT_BC_SAVED   = 46;  // Saved Banners
    static final int SLOT_BC_PREVIEW = 49;  // banner preview    (bottom middle)

    // PATTERN row-5
    static final int SLOT_PT_BACK    = 45;  // Back to Base Colour
    static final int SLOT_PT_SAVED   = 46;  // Saved Banners
    static final int SLOT_PT_SAVE    = 47;  // Save Design
    static final int SLOT_PT_UNDO    = 48;  // Undo last layer
    static final int SLOT_PT_PREVIEW = 49;  // banner preview    (bottom middle)
    static final int SLOT_CONFIRM    = 53;  // Take Banner       (bottom right)

    // PATTERN_COLOR row-5
    static final int SLOT_PC_BACK    = 45;  // Back to Patterns
    static final int SLOT_PC_INFO    = 46;  // step hint
    static final int SLOT_PC_PREVIEW = 49;  // banner preview    (bottom middle)

    // Saved-banners grid (rows 0-3, 36 items per page)
    static final int SAVED_GRID_SIZE = 36;
    static final int SLOT_SB_BACK    = 53;
    static final int SLOT_SB_PREV    = 45;
    static final int SLOT_SB_PAGE    = 49;
    static final int SLOT_SB_NEXT    = 51;

    // -- Static data --
    static final List<DyeColor> DYE_COLORS = List.of(DyeColor.values());

    static final List<PatternType> ALL_PATTERNS;
    static final Map<PatternType, String> PATTERN_NAMES = new HashMap<>();

    static {
        List<PatternType> tmp = new ArrayList<>();
        Registry.BANNER_PATTERN.forEach(tmp::add);
        tmp.remove(PatternType.BASE);
        ALL_PATTERNS = List.copyOf(tmp);

        // Short, descriptive names
        PATTERN_NAMES.put(PatternType.BORDER,                "Border");
        PATTERN_NAMES.put(PatternType.BRICKS,                "Bricks");
        PATTERN_NAMES.put(PatternType.CIRCLE,                "Circle");
        PATTERN_NAMES.put(PatternType.CREEPER,               "Creeper");
        PATTERN_NAMES.put(PatternType.CROSS,                 "Diagonal Cross");
        PATTERN_NAMES.put(PatternType.CURLY_BORDER,          "Curly Border");
        PATTERN_NAMES.put(PatternType.DIAGONAL_LEFT,         "Down-Right Diagonal");
        PATTERN_NAMES.put(PatternType.DIAGONAL_RIGHT,        "Down-Left Diagonal");
        PATTERN_NAMES.put(PatternType.DIAGONAL_UP_LEFT,      "Up-Left Diagonal");
        PATTERN_NAMES.put(PatternType.DIAGONAL_UP_RIGHT,     "Up-Right Diagonal");
        PATTERN_NAMES.put(PatternType.FLOW,                  "Flow");
        PATTERN_NAMES.put(PatternType.FLOWER,                "Flower");
        PATTERN_NAMES.put(PatternType.GLOBE,                 "Globe");
        PATTERN_NAMES.put(PatternType.GRADIENT,              "Gradient Down");
        PATTERN_NAMES.put(PatternType.GRADIENT_UP,           "Gradient Up");
        PATTERN_NAMES.put(PatternType.GUSTER,                "Guster");
        PATTERN_NAMES.put(PatternType.HALF_HORIZONTAL,       "Top Half");
        PATTERN_NAMES.put(PatternType.HALF_HORIZONTAL_BOTTOM,"Bottom Half");
        PATTERN_NAMES.put(PatternType.HALF_VERTICAL,         "Left Half");
        PATTERN_NAMES.put(PatternType.HALF_VERTICAL_RIGHT,   "Right Half");
        PATTERN_NAMES.put(PatternType.MOJANG,                "Mojang Logo");
        PATTERN_NAMES.put(PatternType.PIGLIN,                "Piglin");
        PATTERN_NAMES.put(PatternType.RHOMBUS,               "Rhombus");
        PATTERN_NAMES.put(PatternType.SKULL,                 "Skull");
        PATTERN_NAMES.put(PatternType.SMALL_STRIPES,         "Small Stripes");
        PATTERN_NAMES.put(PatternType.SQUARE_BOTTOM_LEFT,    "Bottom-Left Corner");
        PATTERN_NAMES.put(PatternType.SQUARE_BOTTOM_RIGHT,   "Bottom-Right Corner");
        PATTERN_NAMES.put(PatternType.SQUARE_TOP_LEFT,       "Top-Left Corner");
        PATTERN_NAMES.put(PatternType.SQUARE_TOP_RIGHT,      "Top-Right Corner");
        PATTERN_NAMES.put(PatternType.STRAIGHT_CROSS,        "Square Cross");
        PATTERN_NAMES.put(PatternType.STRIPE_BOTTOM,         "Bottom Stripe");
        PATTERN_NAMES.put(PatternType.STRIPE_CENTER,         "Center Stripe");
        PATTERN_NAMES.put(PatternType.STRIPE_DOWNLEFT,       "Down-Left Stripe");
        PATTERN_NAMES.put(PatternType.STRIPE_DOWNRIGHT,      "Down-Right Stripe");
        PATTERN_NAMES.put(PatternType.STRIPE_LEFT,           "Left Stripe");
        PATTERN_NAMES.put(PatternType.STRIPE_MIDDLE,         "Middle Stripe");
        PATTERN_NAMES.put(PatternType.STRIPE_RIGHT,          "Right Stripe");
        PATTERN_NAMES.put(PatternType.STRIPE_TOP,            "Top Stripe");
        PATTERN_NAMES.put(PatternType.TRIANGLE_BOTTOM,       "Bottom Triangle");
        PATTERN_NAMES.put(PatternType.TRIANGLE_TOP,          "Top Triangle");
        PATTERN_NAMES.put(PatternType.TRIANGLES_BOTTOM,      "Bottom Sawtooth");
        PATTERN_NAMES.put(PatternType.TRIANGLES_TOP,         "Top Sawtooth");
    }

    // -- Instance --
    private final EnchantedLoomPlugin plugin;
    private final GUISession session;

    public EnchantedLoomGUI(EnchantedLoomPlugin plugin, GUISession session) {
        this.plugin  = plugin;
        this.session = session;
    }

    // =========================================================================
    // Public open / transition / redraw
    // =========================================================================

    /** Open (or re-open) a fresh inventory for the current step. */
    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, stepTitle());
        populate(inv);
        session.getPlayer().openInventory(inv);
        session.setTransitioning(false);
        session.setCurrentInventory(inv);
    }

    /**
     * Switch to a new step and open a new titled inventory.
     * Sets transitioning=true before openInventory() so the InventoryCloseEvent
     * for the old inventory does not clean up the session.
     */
    public void transition(GUISession.Step next) {
        session.setStep(next);
        session.setTransitioning(true);
        open();
    }

    /**
     * Redraw the existing inventory in-place (same step -- e.g. pagination).
     * Does not reopen; avoids an extra close/open cycle.
     */
    public void redraw() {
        Inventory inv = session.getCurrentInventory();
        if (inv == null) { open(); return; }
        inv.clear();
        populate(inv);
        session.getPlayer().updateInventory();
    }

    // =========================================================================
    // Population dispatch
    // =========================================================================

    private void populate(Inventory inv) {
        switch (session.getStep()) {
            case BASE_COLOR    -> populateBaseColor(inv);
            case PATTERN       -> populatePattern(inv);
            case PATTERN_COLOR -> populatePatternColor(inv);
            case SAVED_BANNERS -> populateSavedBanners(inv);
        }
        fillGlass(inv);
    }

    // =========================================================================
    // Step 1: BASE_COLOR
    // =========================================================================

    private void populateBaseColor(Inventory inv) {
        // Content area (rows 0-4): colour swatches in rows 1-2
        for (int i = 0; i < DYE_COLORS.size(); i++) {
            DyeColor dye = DYE_COLORS.get(i);
            boolean selected = dye == session.getBaseColor();
            inv.setItem(COLOR_SLOTS[i], buildBaseColorSwatch(dye, selected));
        }

        // Row 5: info | saved banners | (glass) | (glass) | preview | ...
        inv.setItem(SLOT_BC_INFO, makeInfo(
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Choose Base Colour",
                List.of(
                        ChatColor.GRAY + "Click a colour to set the banner background.",
                        ChatColor.YELLOW + "Current: " + formatDyeName(session.getBaseColor())
                )
        ));

        inv.setItem(SLOT_BC_SAVED, makeControl(Material.BOOK,
                ChatColor.AQUA + "" + ChatColor.BOLD + "Saved Banners",
                List.of(ChatColor.GRAY + "Browse and retrieve your saved designs.")));

        inv.setItem(SLOT_BC_PREVIEW, buildPreviewBanner());
    }

    private ItemStack buildBaseColorSwatch(DyeColor dye, boolean selected) {
        ItemStack item = new ItemStack(bannerMaterialFor(dye));
        BannerMeta meta = (BannerMeta) item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(
                    (selected ? ChatColor.GREEN + "" + ChatColor.BOLD : ChatColor.WHITE.toString())
                    + formatDyeName(dye));
            meta.setLore(List.of(selected
                    ? ChatColor.GREEN + ">> Currently selected"
                    : ChatColor.GRAY  + "Click to select"));
            item.setItemMeta(meta);
        }
        return item;
    }

    // =========================================================================
    // Step 2: PATTERN
    // =========================================================================

    private void populatePattern(Inventory inv) {
        int maxLayers = plugin.getConfig().getInt("max-banner-layers", 6);
        boolean atMax = session.getLayers().size() >= maxLayers;

        // Pattern grid -- rows 0-4 (slots 0-44), all patterns fit on one page
        for (int i = 0; i < ALL_PATTERNS.size(); i++) {
            inv.setItem(PATTERN_GRID_START + i, buildPatternGridItem(ALL_PATTERNS.get(i), atMax));
        }

        // Row 5: back | saved banners | save design | undo | preview | (glass) | (glass) | (glass) | take
        inv.setItem(SLOT_PT_BACK, makeControl(Material.SPECTRAL_ARROW,
                ChatColor.YELLOW + "<< Back to Base Colour",
                List.of(
                        ChatColor.GRAY + "Return to the background colour picker.",
                        ChatColor.GRAY + "Layers: " + session.getLayers().size() + "/" + maxLayers
                )));

        inv.setItem(SLOT_PT_SAVED, makeControl(Material.BOOK,
                ChatColor.AQUA + "" + ChatColor.BOLD + "Saved Banners",
                List.of(ChatColor.GRAY + "Browse and retrieve your saved designs.")));

        inv.setItem(SLOT_PT_SAVE, makeControl(Material.WRITABLE_BOOK,
                ChatColor.GREEN + "" + ChatColor.BOLD + "Save Design",
                List.of(
                        ChatColor.GRAY + "Save this design to retrieve later.",
                        ChatColor.GRAY + "You will be asked to type a name in chat."
                )));

        if (!session.getLayers().isEmpty()) {
            inv.setItem(SLOT_PT_UNDO, makeControl(Material.RED_DYE,
                    ChatColor.RED + "Undo Last Layer",
                    List.of(ChatColor.GRAY + "Remove the most recent pattern layer.")));
        }

        inv.setItem(SLOT_PT_PREVIEW, buildPreviewBanner());

        boolean requireBlank = plugin.getConfig().getBoolean("require-blank-banner", true);
        boolean isCreative   = session.getPlayer().getGameMode() == org.bukkit.GameMode.CREATIVE;
        boolean needsBanner  = requireBlank && !isCreative;

        String takenLine = session.getBannersTaken() > 0
                ? ChatColor.DARK_AQUA + "Taken this session: " + session.getBannersTaken()
                : needsBanner
                        ? ChatColor.GRAY + "Consumes one blank "
                                + ChatColor.WHITE + formatDyeName(session.getBaseColor())
                                + ChatColor.GRAY + " banner from your inventory."
                        : ChatColor.GRAY + "Click to receive a copy of this banner.";

        List<String> confirmLore = new ArrayList<>();
        if (needsBanner) {
            confirmLore.add(ChatColor.GRAY + "Requires a blank "
                    + ChatColor.WHITE + formatDyeName(session.getBaseColor())
                    + ChatColor.GRAY + " banner in your inventory.");
        } else if (isCreative) {
            confirmLore.add(ChatColor.AQUA + "Creative mode — no banner required.");
        }
        confirmLore.add(ChatColor.GRAY + "Layers: " + session.getLayers().size());
        confirmLore.add(takenLine);

        inv.setItem(SLOT_CONFIRM, makeControl(Material.EMERALD,
                ChatColor.GREEN + "" + ChatColor.BOLD + "Take Banner",
                confirmLore));
    }

    private ItemStack buildPatternGridItem(PatternType type, boolean atMax) {
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_BANNER);
        BannerMeta meta = (BannerMeta) item.getItemMeta();
        if (meta != null) {
            meta.addPattern(new Pattern(DyeColor.BLACK, type));
            meta.setDisplayName((atMax ? ChatColor.RED : ChatColor.YELLOW) + formatPatternName(type));
            meta.setLore(List.of(atMax
                    ? ChatColor.RED + "Max layers reached!"
                    : ChatColor.GRAY + "Click to choose a colour for this pattern."));
            item.setItemMeta(meta);
        }
        return item;
    }

    // =========================================================================
    // Step 3: PATTERN_COLOR
    // =========================================================================

    private void populatePatternColor(Inventory inv) {
        PatternType pending = session.getPendingPattern();
        String patName = pending != null ? formatPatternName(pending) : "?";

        // Content area (rows 0-4): colour swatches in rows 1-2
        for (int i = 0; i < DYE_COLORS.size(); i++) {
            inv.setItem(COLOR_SLOTS[i], buildPatternColorSwatch(DYE_COLORS.get(i), pending));
        }

        // Row 5: back | info | (glass) | (glass) | preview | ...
        inv.setItem(SLOT_PC_BACK, makeControl(Material.SPECTRAL_ARROW,
                ChatColor.YELLOW + "<< Back to Patterns",
                List.of(
                        ChatColor.GRAY + "Return without adding this layer.",
                        ChatColor.GRAY + "Pattern: " + patName
                )));

        inv.setItem(SLOT_PC_INFO, makeInfo(
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Choose Pattern Colour",
                List.of(
                        ChatColor.GRAY + "Pattern: " + ChatColor.YELLOW + patName,
                        ChatColor.GRAY + "Click a swatch to add this layer."
                )
        ));

        inv.setItem(SLOT_PC_PREVIEW, buildPreviewBanner());
    }

    private ItemStack buildPatternColorSwatch(DyeColor dye, PatternType pending) {
        // Light-gray base + pending pattern in the chosen dye = live preview
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_BANNER);
        BannerMeta meta = (BannerMeta) item.getItemMeta();
        if (meta != null) {
            if (pending != null) meta.addPattern(new Pattern(dye, pending));
            meta.setDisplayName(ChatColor.WHITE + formatDyeName(dye));
            meta.setLore(List.of(ChatColor.GRAY + "Click to add this layer."));
            item.setItemMeta(meta);
        }
        return item;
    }

    // =========================================================================
    // Step 4: SAVED_BANNERS
    // =========================================================================

    private void populateSavedBanners(Inventory inv) {
        List<BannerStorage.SavedBanner> banners = getVisibleBanners();

        int page      = session.getSavedBannersPage();
        int start     = page * SAVED_GRID_SIZE;
        int totalPages = Math.max(1, (int) Math.ceil((double) banners.size() / SAVED_GRID_SIZE));

        if (banners.isEmpty()) {
            inv.setItem(22, makeInfo(
                    ChatColor.GRAY + "No saved banners yet.",
                    List.of(
                            ChatColor.GRAY + "Use the " + ChatColor.GREEN + "Save Design"
                                    + ChatColor.GRAY + " button to save a banner.",
                            ChatColor.GRAY + "Saved designs appear here."
                    )
            ));
        } else {
            for (int i = 0; i < SAVED_GRID_SIZE; i++) {
                int idx = start + i;
                if (idx >= banners.size()) break;
                inv.setItem(i, buildSavedBannerItem(banners.get(idx)));
            }
        }

        // Row 5 navigation
        if (page > 0) {
            inv.setItem(SLOT_SB_PREV, makeNav(false, "page"));
        }
        boolean sbRequireBlank = plugin.getConfig().getBoolean("require-blank-banner", true);
        boolean sbIsCreative   = session.getPlayer().getGameMode() == org.bukkit.GameMode.CREATIVE;
        String clickHint = (sbRequireBlank && !sbIsCreative)
                ? ChatColor.YELLOW + "Left-click: consumes a matching blank banner."
                : ChatColor.GRAY   + "Left-click to get a copy.";
        inv.setItem(SLOT_SB_PAGE, makeInfo(
                ChatColor.YELLOW + "Page " + (page + 1) + " / " + totalPages,
                List.of(
                        ChatColor.GRAY + "Saved designs: " + banners.size(),
                        clickHint,
                        ChatColor.RED  + "Shift-click to delete."
                )
        ));
        if ((page + 1) * SAVED_GRID_SIZE < banners.size()) {
            inv.setItem(SLOT_SB_NEXT, makeNav(true, "page"));
        }
        inv.setItem(SLOT_SB_BACK, makeControl(Material.SPECTRAL_ARROW,
                ChatColor.YELLOW + "<< Back to Design",
                List.of(ChatColor.GRAY + "Return to the banner editor.")));
    }

    private ItemStack buildSavedBannerItem(BannerStorage.SavedBanner saved) {
        boolean isGlobal = isGlobalMode();
        ItemStack banner = new ItemStack(bannerMaterialFor(saved.base()));
        BannerMeta meta  = (BannerMeta) banner.getItemMeta();
        if (meta != null) {
            for (Pattern p : saved.layers()) meta.addPattern(p);
            meta.displayName(MiniMessage.miniMessage().deserialize(saved.displayName()));
            List<String> lore = new ArrayList<>();
            if (isGlobal) {
                String ownerName = org.bukkit.Bukkit.getOfflinePlayer(saved.ownerId()).getName();
                lore.add(ChatColor.DARK_AQUA + "By: " + ChatColor.WHITE
                        + (ownerName != null ? ownerName : saved.ownerId().toString()));
            }
            lore.add(ChatColor.GRAY + "Base: " + ChatColor.WHITE + formatDyeName(saved.base()));
            lore.add(ChatColor.GRAY + "Layers: " + ChatColor.WHITE + saved.layers().size());
            for (int i = 0; i < saved.layers().size(); i++) {
                Pattern p = saved.layers().get(i);
                lore.add("" + ChatColor.DARK_GRAY + (i + 1) + ". "
                        + ChatColor.WHITE + formatPatternName(p.getPattern())
                        + ChatColor.GRAY + " / " + formatDyeName(p.getColor()));
            }
            lore.add("");
            boolean requireBlank = plugin.getConfig().getBoolean("require-blank-banner", true);
            boolean isCreative   = session.getPlayer().getGameMode() == org.bukkit.GameMode.CREATIVE;
            if (requireBlank && !isCreative) {
                lore.add(ChatColor.YELLOW + "Left-click: requires a blank "
                        + ChatColor.WHITE + formatDyeName(saved.base())
                        + ChatColor.YELLOW + " banner.");
            } else {
                lore.add(ChatColor.GREEN + "Left-click to get a copy.");
            }
            boolean adminDelete = plugin.getConfig().getBoolean("allow-admin-delete-banners", false)
                    && session.getPlayer().hasPermission("enchantedloom.admin");
            boolean canDelete = saved.ownerId().equals(session.getPlayer().getUniqueId()) || adminDelete;
            if (canDelete) {
                lore.add(ChatColor.RED + "Shift-click to delete.");
            }
            meta.setLore(lore);
            banner.setItemMeta(meta);
        }
        return banner;
    }

    // =========================================================================
    // Banner builder (used by GUI + GUIListener confirm)
    // =========================================================================

    /** Returns the banner list visible to this player based on global-banners config. */
    public List<BannerStorage.SavedBanner> getVisibleBanners() {
        boolean globalAll      = plugin.getConfig().getBoolean("global-banners", false);
        boolean globalCreative = plugin.getConfig().getBoolean("global-creative-banners", false);
        boolean isCreative     = session.getPlayer().getGameMode() == org.bukkit.GameMode.CREATIVE;
        if (globalAll || (globalCreative && isCreative)) {
            return plugin.getBannerStorage().getAllBanners();
        }
        return new ArrayList<>(plugin.getBannerStorage().getBanners(session.getPlayer().getUniqueId()).values());
    }

    /** Returns true if the player is currently viewing a global (all-players) banner list. */
    public boolean isGlobalMode() {
        boolean globalAll      = plugin.getConfig().getBoolean("global-banners", false);
        boolean globalCreative = plugin.getConfig().getBoolean("global-creative-banners", false);
        boolean isCreative     = session.getPlayer().getGameMode() == org.bukkit.GameMode.CREATIVE;
        return globalAll || (globalCreative && isCreative);
    }

    /** Builds the current banner ItemStack from session state. */
    public ItemStack buildCurrentBanner() {
        ItemStack banner = new ItemStack(bannerMaterialFor(session.getBaseColor()));
        BannerMeta meta  = (BannerMeta) banner.getItemMeta();
        if (meta == null) return banner;
        for (Pattern layer : session.getLayers()) meta.addPattern(layer);
        banner.setItemMeta(meta);
        return banner;
    }

    /** Banner with full lore -- used as the in-GUI preview item. */
    private ItemStack buildPreviewBanner() {
        int maxLayers = plugin.getConfig().getInt("max-banner-layers", 6);
        ItemStack banner = buildCurrentBanner();
        ItemMeta meta = banner.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Banner Preview");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Base: " + ChatColor.WHITE + formatDyeName(session.getBaseColor()));
            lore.add(ChatColor.GRAY + "Layers: " + ChatColor.WHITE
                    + session.getLayers().size() + "/" + maxLayers);
            for (int i = 0; i < session.getLayers().size(); i++) {
                Pattern p = session.getLayers().get(i);
                lore.add("" + ChatColor.DARK_GRAY + (i + 1) + ". "
                        + ChatColor.WHITE + formatPatternName(p.getPattern())
                        + ChatColor.GRAY  + " / " + formatDyeName(p.getColor()));
            }
            meta.setLore(lore);
            banner.setItemMeta(meta);
        }
        return banner;
    }

    // =========================================================================
    // Static accessor API (used by GUIListener and BannerCommand)
    // =========================================================================

    public static int[]             getColorSlots()          { return COLOR_SLOTS; }
    public static int               getPatternGridStart()     { return PATTERN_GRID_START; }
    public static int               getPatternGridSize()      { return PATTERN_GRID_SIZE; }
    // BASE_COLOR row-5
    public static int               getSlotBcInfo()           { return SLOT_BC_INFO; }
    public static int               getSlotBcSaved()          { return SLOT_BC_SAVED; }
    public static int               getSlotBcPreview()        { return SLOT_BC_PREVIEW; }
    // PATTERN row-5
    public static int               getSlotPtBack()           { return SLOT_PT_BACK; }
    public static int               getSlotPtSaved()          { return SLOT_PT_SAVED; }
    public static int               getSlotPtSave()           { return SLOT_PT_SAVE; }
    public static int               getSlotPtUndo()           { return SLOT_PT_UNDO; }
    public static int               getSlotPtPreview()        { return SLOT_PT_PREVIEW; }
    public static int               getSlotConfirm()          { return SLOT_CONFIRM; }
    // PATTERN_COLOR row-5
    public static int               getSlotPcBack()           { return SLOT_PC_BACK; }
    public static int               getSlotPcInfo()           { return SLOT_PC_INFO; }
    public static int               getSlotPcPreview()        { return SLOT_PC_PREVIEW; }
    // SAVED_BANNERS row-5
    public static int               getSlotSbBack()           { return SLOT_SB_BACK; }
    public static int               getSlotSbPrev()           { return SLOT_SB_PREV; }
    public static int               getSlotSbNext()           { return SLOT_SB_NEXT; }
    public static int               getSavedGridSize()        { return SAVED_GRID_SIZE; }
    public static List<PatternType> getAllPatterns()           { return ALL_PATTERNS; }
    public static List<DyeColor>    getDyeColors()            { return DYE_COLORS; }

    // =========================================================================
    // DyeColor -> Material mappings
    // =========================================================================

    public static Material bannerMaterialFor(DyeColor dye) {
        return switch (dye) {
            case WHITE      -> Material.WHITE_BANNER;
            case ORANGE     -> Material.ORANGE_BANNER;
            case MAGENTA    -> Material.MAGENTA_BANNER;
            case LIGHT_BLUE -> Material.LIGHT_BLUE_BANNER;
            case YELLOW     -> Material.YELLOW_BANNER;
            case LIME       -> Material.LIME_BANNER;
            case PINK       -> Material.PINK_BANNER;
            case GRAY       -> Material.GRAY_BANNER;
            case LIGHT_GRAY -> Material.LIGHT_GRAY_BANNER;
            case CYAN       -> Material.CYAN_BANNER;
            case PURPLE     -> Material.PURPLE_BANNER;
            case BLUE       -> Material.BLUE_BANNER;
            case BROWN      -> Material.BROWN_BANNER;
            case GREEN      -> Material.GREEN_BANNER;
            case RED        -> Material.RED_BANNER;
            case BLACK      -> Material.BLACK_BANNER;
        };
    }

    // =========================================================================
    // Item builder helpers
    // =========================================================================

    private ItemStack makeInfo(String name, List<String> lore) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta  = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(new ArrayList<>(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack makeControl(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta  = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(new ArrayList<>(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack makeNav(boolean forward, String context) {
        return makeControl(
                forward ? Material.ARROW : Material.SPECTRAL_ARROW,
                (forward ? ChatColor.YELLOW + "Next " : ChatColor.YELLOW + "Previous ") + context,
                List.of());
    }

    private void fillGlass(Inventory inv) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) { meta.setDisplayName(" "); filler.setItemMeta(meta); }
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null) inv.setItem(i, filler);
        }
    }

    // =========================================================================
    // String helpers
    // =========================================================================

    private String stepTitle() {
        String base = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui-title", "&5&lEnchanted Loom"));
        return switch (session.getStep()) {
            case BASE_COLOR    -> base + ChatColor.GRAY + " >> " + ChatColor.WHITE + "Base Colour";
            case PATTERN       -> base + ChatColor.GRAY + " >> " + ChatColor.WHITE + "Add Pattern";
            case PATTERN_COLOR -> base + ChatColor.GRAY + " >> " + ChatColor.WHITE + "Pattern Colour";
            case SAVED_BANNERS -> base + ChatColor.GRAY + " >> " + ChatColor.WHITE + "Saved Banners";
        };
    }

    public static String formatPatternName(PatternType type) {
        String mapped = PATTERN_NAMES.get(type);
        return mapped != null ? mapped : capitalise(type.toString());
    }

    public static String formatDyeName(DyeColor dye) {
        return capitalise(dye.name());
    }

    private static String capitalise(String raw) {
        StringBuilder sb = new StringBuilder();
        for (String part : raw.toLowerCase().split("[_\\s]+")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }
}
