package de.fabilucius.advancedperks.api.player;

import java.util.List;
import java.util.UUID;

/**
 * Represents perk data for a player.
 * This is a snapshot of the player's perk state at the time of creation.
 */
public class PlayerPerkData {

    private final UUID playerUuid;
    private final String playerName;
    private final List<String> unlockedPerks;
    private final List<String> enabledPerks;
    private final int maxPerks;
    private final boolean dataLoaded;

    public PlayerPerkData(UUID playerUuid, String playerName,
                          List<String> unlockedPerks, List<String> enabledPerks,
                          int maxPerks, boolean dataLoaded) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.unlockedPerks = unlockedPerks;
        this.enabledPerks = enabledPerks;
        this.maxPerks = maxPerks;
        this.dataLoaded = dataLoaded;
    }

    /**
     * Gets the player's UUID
     *
     * @return The UUID
     */
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    /**
     * Gets the player's name
     *
     * @return The player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Gets all perks the player has unlocked/bought
     *
     * @return List of perk identifiers
     */
    public List<String> getUnlockedPerks() {
        return unlockedPerks;
    }

    /**
     * Gets all currently enabled perks
     *
     * @return List of perk identifiers
     */
    public List<String> getEnabledPerks() {
        return enabledPerks;
    }

    /**
     * Gets the maximum number of perks this player can have active
     *
     * @return Max perks or -1 for unlimited
     */
    public int getMaxPerks() {
        return maxPerks;
    }

    /**
     * Checks if the player's data has been fully loaded
     *
     * @return true if data is loaded
     */
    public boolean isDataLoaded() {
        return dataLoaded;
    }

    /**
     * Checks if a specific perk is unlocked
     *
     * @param perkId The perk identifier
     * @return true if unlocked
     */
    public boolean hasPerkUnlocked(String perkId) {
        return unlockedPerks.contains(perkId);
    }

    /**
     * Checks if a specific perk is enabled
     *
     * @param perkId The perk identifier
     * @return true if enabled
     */
    public boolean isPerkEnabled(String perkId) {
        return enabledPerks.contains(perkId);
    }

    /**
     * Gets the number of currently active perks
     *
     * @return Active perk count
     */
    public int getActivePerksCount() {
        return enabledPerks.size();
    }

    /**
     * Checks if the player can enable more perks
     *
     * @return true if more perks can be enabled
     */
    public boolean canEnableMorePerks() {
        if (maxPerks == -1) {
            return true;
        }
        return getActivePerksCount() < maxPerks;
    }

    /**
     * Gets the number of remaining perk slots
     *
     * @return Number of slots or -1 for unlimited
     */
    public int getRemainingSlots() {
        if (maxPerks == -1) {
            return -1;
        }
        return Math.max(0, maxPerks - getActivePerksCount());
    }

    @Override
    public String toString() {
        return "PlayerPerkData{" +
                "playerUuid=" + playerUuid +
                ", playerName='" + playerName + '\'' +
                ", unlockedPerks=" + unlockedPerks.size() +
                ", enabledPerks=" + enabledPerks.size() +
                ", maxPerks=" + maxPerks +
                ", dataLoaded=" + dataLoaded +
                '}';
    }
}