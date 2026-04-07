package com.enchantedloom.gui;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the per-player state for a wizard-style Enchanted Loom GUI session.
 *
 * <p>Steps:
 * <ol>
 *   <li>BASE_COLOR — player picks the banner's background colour.</li>
 *   <li>PATTERN — player picks a pattern from the paginated grid.</li>
 *   <li>PATTERN_COLOR — player picks the colour for that pattern layer.</li>
 * </ol>
 * After PATTERN_COLOR the layer is committed and the wizard loops back to PATTERN
 * until the player clicks "Take Banner".
 */
public class GUISession {

    /** Wizard step */
    public enum Step { BASE_COLOR, PATTERN, PATTERN_COLOR, SAVED_BANNERS }

    private final Player player;

    private Step    step         = Step.BASE_COLOR;
    private boolean transitioning = false;   // true while switching steps (prevents session cleanup)

    // Banner state
    private DyeColor baseColor = DyeColor.WHITE;
    private final List<Pattern> layers = new ArrayList<>();

    // PATTERN step pagination
    private int page = 0;

    // Pattern chosen in PATTERN step, awaiting a colour in PATTERN_COLOR step
    private PatternType pendingPattern = null;

    // Saved banners view pagination
    private int savedBannersPage = 0;

    // How many banner copies have been given this session
    private int bannersTaken = 0;

    // Reference to the currently open Bukkit inventory for in-place redraw
    private Inventory currentInventory;

    public GUISession(Player player) {
        this.player = player;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public Player   getPlayer()          { return player; }

    public Step     getStep()            { return step; }
    public void     setStep(Step s)      { this.step = s; }

    public boolean  isTransitioning()              { return transitioning; }
    public void     setTransitioning(boolean t)    { this.transitioning = t; }

    public DyeColor getBaseColor()                 { return baseColor; }
    public void     setBaseColor(DyeColor c)       { this.baseColor = c; }

    public List<Pattern> getLayers()               { return layers; }
    public void          addLayer(Pattern p)       { layers.add(p); }
    public void          removeLastLayer()         { if (!layers.isEmpty()) layers.remove(layers.size() - 1); }

    public int  getPage()           { return page; }
    public void setPage(int page)   { this.page = page; }

    public PatternType getPendingPattern()              { return pendingPattern; }
    public void        setPendingPattern(PatternType p) { this.pendingPattern = p; }

    public int  getSavedBannersPage()      { return savedBannersPage; }
    public void setSavedBannersPage(int p) { this.savedBannersPage = p; }

    public int  getBannersTaken()          { return bannersTaken; }
    public void incrementBannersTaken()    { this.bannersTaken++; }

    public Inventory getCurrentInventory()              { return currentInventory; }
    public void      setCurrentInventory(Inventory inv) { this.currentInventory = inv; }
}
