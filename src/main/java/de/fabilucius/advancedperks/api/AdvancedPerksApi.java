package de.fabilucius.advancedperks.api;

import de.fabilucius.advancedperks.perk.Perk;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Main API class for AdvancedPerks plugin.
 * Provides comprehensive methods to interact with the perk system.
 *
 * @author Fabilucius
 * @version 3.4.1
 */
public final class AdvancedPerksAPI {

    private static AdvancedPerksAPI instance;
    private final de.fabilucius.advancedperks.AdvancedPerks plugin;
    private final de.fabilucius.advancedperks.api.manager.PerkManager perkManager;
    private static de.fabilucius.advancedperks.registry.PerkRegistry perkRegistry;
    private static de.fabilucius.advancedperks.data.PerkDataRepository perkDataRepository;
    private final de.fabilucius.advancedperks.data.state.PerkStateController perkStateController;

    /**
     * Private constructor to ensure singleton pattern.
     */
    private AdvancedPerksAPI() {
        this.plugin = JavaPlugin.getPlugin(de.fabilucius.advancedperks.AdvancedPerks.class);
        this.perkManager = plugin.getPerkManager();
        AdvancedPerksAPI.perkRegistry = plugin.getInjector().getInstance(de.fabilucius.advancedperks.registry.PerkRegistry.class);
        AdvancedPerksAPI.perkDataRepository = plugin.getInjector().getInstance(de.fabilucius.advancedperks.data.PerkDataRepository.class);
        this.perkStateController = plugin.getInjector().getInstance(de.fabilucius.advancedperks.data.state.PerkStateController.class);
    }

    /**
     * Gets the API instance.
     *
     * @return The API instance
     */
    public static AdvancedPerksAPI getInstance() {
        if (instance == null) {
            instance = new AdvancedPerksAPI();
        }
        return instance;
    }

    /**
     * Checks if the API is available and ready to use.
     *
     * @return true if API is available, false otherwise
     */
    public static boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("AdvancedPerks");
    }

    // ========== PERK MANAGEMENT METHODS ==========

    /**
     * Gets all registered perks.
     *
     * @return Unmodifiable list of all perks
     */
    public List<Perk> getAllPerks() {
        return Collections.unmodifiableList(perkRegistry.getPerks());
    }

    /**
     * Gets all enabled perks (enabled in configuration).
     *
     * @return List of enabled perks
     */
    public List<Perk> getEnabledPerks() {
        return perkRegistry.getPerks().stream()
                .filter(Perk::isEnabled)
                .toList();
    }

    /**
     * Gets a perk by its identifier.
     *
     * @param identifier The perk identifier
     * @return The perk or null if not found
     */
    public static Perk getPerk(String identifier) {
        return perkRegistry.getPerkByIdentifier(identifier);
    }

    /**
     * Gets a perk by its class.
     *
     * @param perkClass The perk class
     * @return The perk instance or null if not found
     */
    public <T extends Perk> T getPerk(Class<T> perkClass) {
        return perkRegistry.getPerk(perkClass);
    }

    // ========== PLAYER PERK METHODS ==========

    /**
     * Gets all perks that a player has unlocked.
     *
     * @param player The player
     * @return List of unlocked perks
     */
    public List<Perk> getUnlockedPerks(Player player) {
        de.fabilucius.advancedperks.data.PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);
        return perkData.getBoughtPerks().stream()
                .map(perkRegistry::getPerkByIdentifier)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Alias für hasPerkUnlocked – prüft, ob ein Spieler einen Perk freigeschaltet hat.
     *
     * @param player   Der Spieler
     * @param perkId   Die Perk-ID
     * @return true, wenn freigeschaltet, sonst false
     */
    public static boolean isPerkUnlocked(Player player, String perkId) {
        return hasPerkUnlocked(player, perkId);
    }

    /**
     * Gets all currently enabled perks for a player.
     *
     * @param player The player
     * @return List of enabled perks
     */
    public List<Perk> getEnabledPerks(Player player) {
        de.fabilucius.advancedperks.data.PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);
        return List.copyOf(perkData.getEnabledPerks());
    }

    /**
     * Checks if a player has permission to use a perk.
     *
     * @param player The player
     * @param perkId The perk identifier
     * @return true if player has permission, false otherwise
     */
    public boolean hasPermission(Player player, String perkId) {
        Perk perk = getPerk(perkId);
        if (perk == null) {
            return false;
        }

        return perkStateController.canUsePerk(player, perk) ==
                de.fabilucius.advancedperks.data.state.PerkUseStatus.CAN_BE_USED;
    }

    /**
     * Checks if a player has unlocked a perk.
     *
     * @param player The player
     * @param perkId The perk identifier
     * @return true if unlocked, false otherwise
     */
    public static boolean hasPerkUnlocked(Player player, String perkId) {
        de.fabilucius.advancedperks.data.PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);
        return perkData.getBoughtPerks().contains(perkId);
    }

    /**
     * Checks if a perk is currently enabled for a player.
     *
     * @param player The player
     * @param perkId The perk identifier
     * @return true if enabled, false otherwise
     */
    public static boolean isPerkEnabled(Player player, String perkId) {
        Perk perk = getPerk(perkId);
        if (perk == null) {
            return false;
        }

        de.fabilucius.advancedperks.data.PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);
        return perkData.isPerkEnabled(perk);
    }

    // ========== PERK ACTIONS ==========

    /**
     * Enables a perk for a player.
     *
     * @param player The player
     * @param perkId The perk identifier
     * @return Result of the operation
     */
    public PerkActionResult enablePerk(Player player, String perkId) {
        Perk perk = getPerk(perkId);
        if (perk == null) {
            return PerkActionResult.PERK_NOT_FOUND;
        }

        de.fabilucius.advancedperks.data.state.PerkToggleResult result =
                perkStateController.enablePerk(player, perk);

        return convertToggleResult(result);
    }

    /**
     * Disables a perk for a player.
     *
     * @param player The player
     * @param perkId The perk identifier
     * @return Result of the operation
     */
    public PerkActionResult disablePerk(Player player, String perkId) {
        Perk perk = getPerk(perkId);
        if (perk == null) {
            return PerkActionResult.PERK_NOT_FOUND;
        }

        de.fabilucius.advancedperks.data.state.PerkToggleResult result =
                perkStateController.disablePerk(player, perk);

        return convertToggleResult(result);
    }

    /**
     * Toggles a perk for a player.
     *
     * @param player The player
     * @param perkId The perk identifier
     * @return Result of the operation
     */
    public PerkActionResult togglePerk(Player player, String perkId) {
        Perk perk = getPerk(perkId);
        if (perk == null) {
            return PerkActionResult.PERK_NOT_FOUND;
        }

        de.fabilucius.advancedperks.data.state.PerkToggleResult result =
                perkStateController.togglePerk(player, perk);

        return convertToggleResult(result);
    }

    /**
     * Forces a perk to be enabled for a player (bypasses checks).
     *
     * @param player The player
     * @param perkId The perk identifier
     * @return Result of the operation
     */
    public PerkActionResult forceEnablePerk(Player player, String perkId) {
        Perk perk = getPerk(perkId);
        if (perk == null) {
            return PerkActionResult.PERK_NOT_FOUND;
        }

        de.fabilucius.advancedperks.data.state.PerkToggleResult result =
                perkStateController.forceEnablePerk(player, perk);

        return convertToggleResult(result);
    }

    /**
     * Forces a perk to be disabled for a player.
     *
     * @param player The player
     * @param perkId The perk identifier
     * @return Result of the operation
     */
    public PerkActionResult forceDisablePerk(Player player, String perkId) {
        Perk perk = getPerk(perkId);
        if (perk == null) {
            return PerkActionResult.PERK_NOT_FOUND;
        }

        de.fabilucius.advancedperks.data.state.PerkToggleResult result =
                perkStateController.forceDisablePerk(player, perk);

        return convertToggleResult(result);
    }

    /**
     * Disables all perks for a player.
     *
     * @param player The player
     */
    public void disableAllPerks(Player player) {
        perkStateController.disableAllPerks(player);
    }

    // ========== ECONOMY METHODS ==========

    /**
     * Attempts to buy a perk for a player.
     *
     * @param player The player
     * @param perkId The perk identifier
     * @return Result of the purchase
     */
    public PerkBuyResult buyPerk(Player player, String perkId) {
        Perk perk = getPerk(perkId);
        if (perk == null) {
            return PerkBuyResult.PERK_NOT_FOUND;
        }

        de.fabilucius.advancedperks.core.economy.EconomyController economyController =
                plugin.getInjector().getInstance(de.fabilucius.advancedperks.core.economy.EconomyController.class);

        de.fabilucius.advancedperks.core.economy.PerkBuyResult result =
                economyController.buyPerk(player, perk);

        return switch (result) {
            case SUCCESS -> PerkBuyResult.SUCCESS;
            case ALREADY_BOUGHT_PERK -> PerkBuyResult.ALREADY_BOUGHT;
            case NOT_ENOUGH_FUNDS -> PerkBuyResult.NOT_ENOUGH_MONEY;
            case NO_PRICE_SET -> PerkBuyResult.NO_PRICE_SET;
            case NO_ECONOMY_INTERFACE -> PerkBuyResult.NO_ECONOMY;
            default -> PerkBuyResult.ERROR;
        };
    }

    /**
     * Gets the price of a perk.
     *
     * @param perkId The perk identifier
     * @return The price or -1 if no price is set
     */
    public double getPerkPrice(String perkId) {
        Perk perk = getPerk(perkId);
        if (perk == null) {
            return -1;
        }

        return perk.getPrice().orElse(-1.0);
    }

    /**
     * Sets the price of a perk.
     *
     * @param perkId The perk identifier
     * @param price The new price
     * @return true if successful, false otherwise
     */
    public boolean setPerkPrice(String perkId, double price) {
        Perk perk = getPerk(perkId);
        if (perk == null) {
            return false;
        }

        de.fabilucius.advancedperks.registry.model.SetPriceResult result =
                ((de.fabilucius.advancedperks.registry.PerkRegistryImpl) perkRegistry).setPrice(perk, price);

        return result == de.fabilucius.advancedperks.registry.model.SetPriceResult.PRICE_SET;
    }

    // ========== UTILITY METHODS ==========

    /**
     * Gets the maximum number of perks a player can have active.
     *
     * @param player The player
     * @return Maximum number of active perks
     */
    public int getMaxActivePerks(Player player) {
        de.fabilucius.advancedperks.data.PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);
        return perkData.getMaxPerks();
    }

    /**
     * Gets the number of currently active perks for a player.
     *
     * @param player The player
     * @return Number of active perks
     */
    public int getActivePerksCount(Player player) {
        de.fabilucius.advancedperks.data.PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);
        return perkData.getEnabledPerks().size();
    }

    /**
     * Checks if a player can enable more perks.
     *
     * @param player The player
     * @return true if player can enable more perks, false otherwise
     */
    public boolean canEnableMorePerks(Player player) {
        int max = getMaxActivePerks(player);
        if (max == -1) {
            return true; // No limit
        }

        return getActivePerksCount(player) < max;
    }

    /**
     * Reloads the perk data for a player.
     *
     * @param player The player
     */
    public void reloadPlayerData(Player player) {
        perkDataRepository.getPerkDataByUuid(player.getUniqueId());
    }

    // ========== HELPER METHODS ==========

    private PerkActionResult convertToggleResult(de.fabilucius.advancedperks.data.state.PerkToggleResult result) {
        return switch (result) {
            case ENABLED -> PerkActionResult.SUCCESS_ENABLED;
            case DISABLED -> PerkActionResult.SUCCESS_DISABLED;
            case NO_PERMISSION -> PerkActionResult.NO_PERMISSION;
            case TOO_MANY_ACTIVE -> PerkActionResult.TOO_MANY_ACTIVE;
            case DISALLOWED_WORLD -> PerkActionResult.DISALLOWED_WORLD;
            case EVENT_CANCELLED -> PerkActionResult.EVENT_CANCELLED;
            default -> PerkActionResult.ERROR;
        };
    }

    /**
     * Result of a perk action.
     */
    public enum PerkActionResult {
        SUCCESS_ENABLED,
        SUCCESS_DISABLED,
        PERK_NOT_FOUND,
        NO_PERMISSION,
        TOO_MANY_ACTIVE,
        DISALLOWED_WORLD,
        EVENT_CANCELLED,
        ERROR
    }

    /**
     * Result of a perk purchase.
     */
    public enum PerkBuyResult {
        SUCCESS,
        PERK_NOT_FOUND,
        ALREADY_BOUGHT,
        NOT_ENOUGH_MONEY,
        NO_PRICE_SET,
        NO_ECONOMY,
        ERROR
    }
}