package de.fabilucius.advancedperks.api.manager;

import com.google.inject.Inject;
import de.fabilucius.advancedperks.AdvancedPerks;
import de.fabilucius.advancedperks.data.PerkData;
import de.fabilucius.advancedperks.data.PerkDataRepository;
import de.fabilucius.advancedperks.data.state.PerkStateController;
import de.fabilucius.advancedperks.perk.Perk;
import de.fabilucius.advancedperks.registry.PerkRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages player perks: unlocking, enabling, disabling, and persistence.
 */
public class PerkManager {
    private final AdvancedPerks plugin;
    private final Map<UUID, Set<String>> unlocked = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> enabled = new ConcurrentHashMap<>();
    private boolean initialized = false;

    @Inject
    private PerkDataRepository perkDataRepository;

    @Inject
    private PerkStateController perkStateController;

    @Inject
    private PerkRegistry perkRegistry;

    @Inject
    public PerkManager(AdvancedPerks plugin) {
        this.plugin = plugin;
        // Don't call loadPersistedData() here - dependencies aren't injected yet
    }

    /**
     * Initializes the PerkManager after all dependencies are injected.
     * This method should be called manually after Guice injection is complete.
     */
    public void initialize() {
        if (!initialized) {
            loadPersistedData();
            initialized = true;
        }
    }

    /**
     * Ensures the manager is initialized before performing operations.
     */
    private void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }

    /**
     * Loads persisted data from the main perk data repository into local caches.
     */
    private void loadPersistedData() {
        try {
            // Only load if we're not in a test environment
            if (perkDataRepository != null) {
                // Load data for all online players
                for (Player player : Bukkit.getOnlinePlayers()) {
                    loadPlayerData(player);
                }
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger();
                plugin.getLogger().log(Level.WARNING, "Failed to load persisted perk data", e);
            }
        }
    }

    /**
     * Loads data for a specific player from the repository.
     *
     * @param player The player to load data for
     */
    private void loadPlayerData(Player player) {
        if (perkDataRepository == null) {
            return; // Skip in test environment
        }

        UUID uuid = player.getUniqueId();
        PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);

        // Load unlocked perks
        Set<String> unlockedPerks = ConcurrentHashMap.newKeySet();
        unlockedPerks.addAll(perkData.getBoughtPerks());
        unlocked.put(uuid, unlockedPerks);

        // Load enabled perks
        Set<String> enabledPerks = ConcurrentHashMap.newKeySet();
        perkData.getEnabledPerks().forEach(perk -> enabledPerks.add(perk.getIdentifier()));
        enabled.put(uuid, enabledPerks);
    }

    public boolean hasUnlocked(Player player, String perkId) {
        ensureInitialized();
        UUID uuid = player.getUniqueId();

        // Ensure data is loaded for this player
        if (!unlocked.containsKey(uuid)) {
            loadPlayerData(player);
        }

        return unlocked.getOrDefault(uuid, Collections.emptySet()).contains(perkId);
    }

    public boolean isEnabled(Player player, String perkId) {
        ensureInitialized();
        UUID uuid = player.getUniqueId();

        // Ensure data is loaded for this player
        if (!enabled.containsKey(uuid)) {
            loadPlayerData(player);
        }

        return enabled.getOrDefault(uuid, Collections.emptySet()).contains(perkId);
    }

    public boolean unlockPerk(Player player, String perkId) {
        ensureInitialized();
        UUID uuid = player.getUniqueId();
        unlocked.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
        Set<String> perks = unlocked.get(uuid);

        if (perks.add(perkId)) {
            // Update the main perk data repository
            if (perkDataRepository != null) {
                PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);
                perkData.getBoughtPerks().add(perkId);
            }

            persistPlayerData(player);
            return true;
        }
        return false;
    }

    public boolean enablePerk(Player player, String perkId) {
        ensureInitialized();
        if (!hasUnlocked(player, perkId)) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        enabled.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
        Set<String> perks = enabled.get(uuid);

        if (perks.add(perkId)) {
            applyPerkEffects(player, perkId);
            persistPlayerData(player);
            return true;
        }
        return false;
    }

    public boolean disablePerk(Player player, String perkId) {
        ensureInitialized();
        UUID uuid = player.getUniqueId();
        Set<String> perks = enabled.getOrDefault(uuid, Collections.emptySet());

        if (perks.remove(perkId)) {
            removePerkEffects(player, perkId);
            persistPlayerData(player);
            return true;
        }
        return false;
    }

    /**
     * Applies perk effects to the player by delegating to the perk state controller.
     *
     * @param player The player to apply effects to
     * @param perkId The perk identifier
     */
    private void applyPerkEffects(Player player, String perkId) {
        try {
            if (perkRegistry == null || perkStateController == null) {
                return; // Skip in test environment
            }

            Perk perk = perkRegistry.getPerkByIdentifier(perkId);
            if (perk != null) {
                // Use the main perk state controller to enable the perk
                perkStateController.forceEnablePerk(player, perk);
            } else {
                if (plugin != null) {
                    plugin.getLogger();
                    plugin.getLogger().log(Level.WARNING, () -> "Attempted to apply effects for unknown perk: " + perkId);
                }
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger();
                plugin.getLogger().log(Level.WARNING, e, () -> "Failed to apply perk effects for " + perkId);
            }
        }
    }

    /**
     * Removes perk effects from the player by delegating to the perk state controller.
     *
     * @param player The player to remove effects from
     * @param perkId The perk identifier
     */
    private void removePerkEffects(Player player, String perkId) {
        try {
            if (perkRegistry == null || perkStateController == null) {
                return; // Skip in test environment
            }

            Perk perk = perkRegistry.getPerkByIdentifier(perkId);
            if (perk != null) {
                // Use the main perk state controller to disable the perk
                perkStateController.forceDisablePerk(player, perk);
            } else {
                if (plugin != null) {
                    plugin.getLogger();
                    plugin.getLogger().log(Level.WARNING, () -> "Attempted to remove effects for unknown perk: " + perkId);
                }
            }
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger();
                plugin.getLogger().log(Level.WARNING, e, () -> "Failed to remove perk effects for " + perkId);
            }
        }
    }

    /**
     * Persists player data by delegating to the main perk data repository.
     *
     * @param player The player whose data should be persisted
     */
    private void persistPlayerData(Player player) {
        try {
            if (perkDataRepository == null) {
                return; // Skip in test environment
            }

            // The main repository handles persistence automatically
            // We just need to ensure our local cache is synchronized
            PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);

            // Sync unlocked perks
            UUID uuid = player.getUniqueId();
            Set<String> localUnlocked = unlocked.get(uuid);
            if (localUnlocked != null) {
                perkData.getBoughtPerks().clear();
                perkData.getBoughtPerks().addAll(localUnlocked);
            }

            // The repository will handle saving asynchronously
            perkDataRepository.savePerkDataAsync(perkData);

        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger();
                plugin.getLogger().log(Level.SEVERE, e, () -> "Failed to persist player data for " + player.getName());
            }
        }
    }

    /**
     * Cleans up data for a player who has left the server.
     *
     * @param player The player who left
     */
    public void cleanupPlayerData(Player player) {
        ensureInitialized();
        UUID uuid = player.getUniqueId();
        unlocked.remove(uuid);
        enabled.remove(uuid);
    }

    /**
     * Gets all unlocked perks for a player.
     *
     * @param player The player
     * @return Set of unlocked perk identifiers
     */
    public Set<String> getUnlockedPerks(Player player) {
        ensureInitialized();
        UUID uuid = player.getUniqueId();
        if (!unlocked.containsKey(uuid)) {
            loadPlayerData(player);
        }
        return Collections.unmodifiableSet(unlocked.getOrDefault(uuid, Collections.emptySet()));
    }

    /**
     * Gets all enabled perks for a player.
     *
     * @param player The player
     * @return Set of enabled perk identifiers
     */
    public Set<String> getEnabledPerks(Player player) {
        ensureInitialized();
        UUID uuid = player.getUniqueId();
        if (!enabled.containsKey(uuid)) {
            loadPlayerData(player);
        }
        return Collections.unmodifiableSet(enabled.getOrDefault(uuid, Collections.emptySet()));
    }
}