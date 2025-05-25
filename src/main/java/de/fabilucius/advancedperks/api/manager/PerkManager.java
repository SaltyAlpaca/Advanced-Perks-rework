package de.fabilucius.advancedperks.api.manager;

import de.fabilucius.advancedperks.AdvancedPerks;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player perks: unlocking, enabling, disabling, and persistence.
 */
public class PerkManager {
    private final AdvancedPerks plugin;
    private final Map<UUID, Set<String>> unlocked = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> enabled = new ConcurrentHashMap<>();

    public PerkManager(AdvancedPerks plugin) {
        this.plugin = plugin;
        // TODO: load persisted data into 'unlocked' and 'enabled'
    }

    public boolean hasUnlocked(Player player, String perkId) {
        return unlocked.getOrDefault(player.getUniqueId(), Collections.emptySet()).contains(perkId);
    }

    public boolean isEnabled(Player player, String perkId) {
        return enabled.getOrDefault(player.getUniqueId(), Collections.emptySet()).contains(perkId);
    }

    public boolean unlockPerk(Player player, String perkId) {
        UUID uuid = player.getUniqueId();
        unlocked.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
        Set<String> perks = unlocked.get(uuid);
        if (perks.add(perkId)) {
            persistPlayerData(player);
            return true;
        }
        return false;
    }

    public boolean enablePerk(Player player, String perkId) {
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
        UUID uuid = player.getUniqueId();
        Set<String> perks = enabled.getOrDefault(uuid, Collections.emptySet());
        if (perks.remove(perkId)) {
            removePerkEffects(player, perkId);
            persistPlayerData(player);
            return true;
        }
        return false;
    }

    private void applyPerkEffects(Player player, String perkId) {
        // TODO: implement effect application, e.g., grant potion effects, listeners
    }

    private void removePerkEffects(Player player, String perkId) {
        // TODO: implement effect removal
    }

    private void persistPlayerData(Player player) {
        // TODO: save 'unlocked' and 'enabled' sets to storage (file or database)
    }
}