package de.fabilucius.advancedperks.perk.defaultperks.listener;

import com.google.inject.Inject;
import de.fabilucius.advancedperks.AdvancedPerks;
import de.fabilucius.advancedperks.data.PerkData;
import de.fabilucius.advancedperks.data.PerkDataRepository;
import de.fabilucius.advancedperks.perk.AbstractDefaultPerk;
import de.fabilucius.advancedperks.perk.annotation.PerkIdentifier;
import de.fabilucius.advancedperks.perk.properties.PerkDescription;
import de.fabilucius.advancedperks.perk.properties.PerkGuiIcon;
import de.fabilucius.advancedperks.perk.types.ListenerPerk;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@PerkIdentifier("double_jump")
public class DoubleJumpPerk extends AbstractDefaultPerk implements ListenerPerk, Listener {

    private static final long COOLDOWN_MS = 3000; // 3 seconds in milliseconds
    private static final String BIRD_PERK_IDENTIFIER = "bird"; // Identifier for the bird perk

    @Inject
    private PerkDataRepository perkDataRepository;

    @Inject
    private AdvancedPerks advancedPerks;

    private final Map<UUID, Long> lastJumpTime = new HashMap<>();
    private final Map<UUID, Boolean> perkFlightEnabled = new HashMap<>(); // Track if flight was enabled by the perk

    public DoubleJumpPerk(String identifier, String displayName, PerkDescription perkDescription, PerkGuiIcon perkGuiIcon, boolean enabled, Map<String, Object> flags) {
        super(identifier, displayName, perkDescription, perkGuiIcon, enabled, flags);
    }

    // Track when a player lands to reset jump capability or disable flight if perk is disabled
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Retrieve player's perk data
        PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);

        // Disable perk flight if perk is not enabled or if bird perk is active
        if (!perkData.getEnabledPerks().contains(this) || perkData.getEnabledPerks().stream().anyMatch(perk -> perk.getIdentifier().equals(BIRD_PERK_IDENTIFIER))) {
            if (Boolean.TRUE.equals(perkFlightEnabled.get(playerId))) {
                player.setAllowFlight(false); // Remove only perk-granted flight
                perkFlightEnabled.put(playerId, false);
            }
            return;
        }

        // Allow flight only if player is grounded, not in creative mode, and perk is active
        if (player.getGameMode() != GameMode.CREATIVE && Math.abs(player.getVelocity().getY()) < 0.1) {
            long currentTime = System.currentTimeMillis();
            long lastTime = lastJumpTime.getOrDefault(playerId, 0L);
            if (currentTime - lastTime >= COOLDOWN_MS && !player.getAllowFlight()) {
                player.setAllowFlight(true); // Allow flight for double jump
                perkFlightEnabled.put(playerId, true);
            }
        }
    }

    // Handle the actual double jump when player toggles flight
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Skip if player is in creative mode or if flight was not enabled by the perk
        if (player.getGameMode() == GameMode.CREATIVE || !Boolean.TRUE.equals(perkFlightEnabled.get(playerId))) {
            return;
        }

        // Check if perk is enabled and bird perk is inactive
        PerkData perkData = this.perkDataRepository.getPerkDataByPlayer(player);
        if (!perkData.getEnabledPerks().contains(this) || perkData.getEnabledPerks().stream().anyMatch(perk -> perk.getIdentifier().equals(BIRD_PERK_IDENTIFIER))) {
            return;
        }

        // Immediately disable perk-granted flight
        player.setAllowFlight(false);
        player.setFlying(false);
        perkFlightEnabled.put(playerId, false);

        // Check cooldown
        long currentTime = System.currentTimeMillis();
        long lastTime = lastJumpTime.getOrDefault(playerId, 0L);
        if (currentTime - lastTime < COOLDOWN_MS) {
            return; // Cooldown not finished, do nothing
        }

        // Update last jump time
        lastJumpTime.put(playerId, currentTime);

        // Cancel flight toggle and apply jump boost
        event.setCancelled(true);

        // Add upward velocity for double jump effect
        Vector jumpVelocity = player.getLocation().getDirection().multiply(0.5).setY(1);
        player.setVelocity(jumpVelocity);

        // Optionally play a sound or particle effect here
        player.getWorld().playSound(player.getLocation(), "entity.firework_rocket.launch", 1, 1);
    }
}
