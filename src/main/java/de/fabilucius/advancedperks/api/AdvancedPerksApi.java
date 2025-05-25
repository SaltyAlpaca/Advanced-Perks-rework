package de.fabilucius.advancedperks.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import de.fabilucius.advancedperks.AdvancedPerks;
import de.fabilucius.advancedperks.api.manager.PerkManager;
import de.fabilucius.advancedperks.api.event.PerkEnableEvent;
import de.fabilucius.advancedperks.api.event.PerkDisableEvent;
import de.fabilucius.advancedperks.api.event.PerkUnlockEvent;
import de.fabilucius.advancedperks.api.event.PerkToggleEvent;

/**
 * Developer API for AdvancedPerks plugin.
 * Provides methods to query and manipulate player perks programmatically.
 */
public final class AdvancedPerksAPI {
    private static final AdvancedPerks plugin = JavaPlugin.getPlugin(AdvancedPerks.class);
    public static final PerkManager perkManager = plugin.getPerkManager();

    private AdvancedPerksAPI() {
        // Prevent instantiation
    }

    /**
     * Checks if a player has unlocked a given perk.
     * @param player the player to check
     * @param perkId the unique ID of the perk
     * @return true if unlocked, false otherwise
     */
    public static boolean isPerkUnlocked(Player player, String perkId) {
        return perkManager.hasUnlocked(player, perkId);
    }

    /**
     * Checks if a perk is currently enabled for a player.
     * @param player the player to check
     * @param perkId the unique ID of the perk
     * @return true if enabled, false otherwise
     */
    public static boolean isPerkEnabled(Player player, String perkId) {
        return perkManager.isEnabled(player, perkId);
    }

    /**
     * Unlocks (grants) a perk for a player. Fires PerkUnlockEvent on success.
     * @param player the player to unlock the perk for
     * @param perkId the unique ID of the perk
     * @return true if perk was newly unlocked, false if already unlocked
     */
    public static boolean unlockPerk(Player player, String perkId) {
        if (!perkManager.hasUnlocked(player, perkId)) {
            boolean success = perkManager.unlockPerk(player, perkId);
            if (success) {
                Bukkit.getPluginManager().callEvent(new PerkUnlockEvent(player, perkId));
            }
            return success;
        }
        return false;
    }

    /**
     * Enables a perk for a player. Fires PerkEnableEvent on success.
     * @param player the player to enable the perk for
     * @param perkId the unique ID of the perk
     * @return true if the perk was enabled, false otherwise
     */
    public static boolean enablePerk(Player player, String perkId) {
        if (perkManager.hasUnlocked(player, perkId) && !perkManager.isEnabled(player, perkId)) {
            boolean success = perkManager.enablePerk(player, perkId);
            if (success) {
                Bukkit.getPluginManager().callEvent(new PerkEnableEvent(player, perkId));
            }
            return success;
        }
        return false;
    }

    /**
     * Disables a perk for a player. Fires PerkDisableEvent on success.
     * @param player the player to disable the perk for
     * @param perkId the unique ID of the perk
     * @return true if the perk was disabled, false otherwise
     */
    public static boolean disablePerk(Player player, String perkId) {
        if (perkManager.isEnabled(player, perkId)) {
            boolean success = perkManager.disablePerk(player, perkId);
            if (success) {
                Bukkit.getPluginManager().callEvent(new PerkDisableEvent(player, perkId));
            }
            return success;
        }
        return false;
    }

    /**
     * Toggles a perk's enabled state for a player. Fires PerkToggleEvent on success.
     * @param player the player to toggle the perk for
     * @param perkId the unique ID of the perk
     * @return true if the state was changed, false otherwise
     */
    public static boolean togglePerk(Player player, String perkId) {
        boolean currentlyEnabled = perkManager.isEnabled(player, perkId);
        boolean success = currentlyEnabled ? disablePerk(player, perkId) : enablePerk(player, perkId);
        if (success) {
            Bukkit.getPluginManager().callEvent(new PerkToggleEvent(player, perkId, !currentlyEnabled));
        }
        return success;
    }
}