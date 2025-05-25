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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@PerkIdentifier("double_jump")
public class DoubleJumpPerk extends AbstractDefaultPerk implements ListenerPerk {

    private static final long COOLDOWN_MS = 3000; // 3 seconds
    private static final double JUMP_POWER = 1.2;
    private static final double FORWARD_MULTIPLIER = 0.5;

    @Inject
    private PerkDataRepository perkDataRepository;
    @Inject
    private AdvancedPerks advancedPerks;

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Boolean> wasOnGround = new HashMap<>();

    public DoubleJumpPerk(String identifier, String displayName, PerkDescription perkDescription,
                          PerkGuiIcon perkGuiIcon, boolean enabled, Map<String, Object> flags) {
        super(identifier, displayName, perkDescription, perkGuiIcon, enabled, flags);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (hasPerkEnabled(player)) {
            Bukkit.getScheduler().runTaskLater(advancedPerks, () -> updateFlightState(player), 1L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cooldowns.remove(uuid);
        wasOnGround.remove(uuid);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!hasPerkEnabled(player)) {
            return;
        }

        boolean onGround = isPlayerOnGround(player);
        UUID uuid = player.getUniqueId();

        // Update flight permission when touching ground
        if (onGround && !Boolean.TRUE.equals(wasOnGround.get(uuid))) {
            updateFlightState(player);
        }

        wasOnGround.put(uuid, onGround);
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        // Ignore creative/spectator mode
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Only handle if perk is enabled
        if (!hasPerkEnabled(player)) {
            return;
        }

        event.setCancelled(true);

        // Always immediately disable flight to prevent hovering
        player.setAllowFlight(false);
        player.setFlying(false);

        // Check cooldown
        if (isOnCooldown(player)) {
            player.sendMessage("§cDouble Jump ist noch im Cooldown!");
            // Don't re-enable flight at all during cooldown
            return;
        }

        // Perform double jump
        performDoubleJump(player);
        setCooldown(player);

        // Re-enable flight after delay only if not on ground
        Bukkit.getScheduler().runTaskLater(advancedPerks, () -> {
            if (hasPerkEnabled(player) && !isPlayerOnGround(player)) {
                updateFlightState(player);
            }
        }, 15L);
    }

    @Override
    public void onPerkEnable(Player player) {
        updateFlightState(player);
    }

    @Override
    public void onPerkDisable(Player player) {
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
        cooldowns.remove(player.getUniqueId());
        wasOnGround.remove(player.getUniqueId());
    }

    private void performDoubleJump(Player player) {
        Vector velocity = player.getLocation().getDirection().multiply(FORWARD_MULTIPLIER);
        velocity.setY(JUMP_POWER);
        player.setVelocity(velocity);

        // Effects
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.3f, 1.5f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 0.5, 0),
                15, 0.3, 0.1, 0.3, 0.05);
    }

    private void updateFlightState(Player player) {
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR && hasPerkEnabled(player)) {
            player.setAllowFlight(true);
            player.setFlying(false);
        }
    }

    private boolean isPlayerOnGround(Player player) {
        Location loc = player.getLocation();

        // Check multiple points around player's feet
        for (double x = -0.3; x <= 0.3; x += 0.3) {
            for (double z = -0.3; z <= 0.3; z += 0.3) {
                Location checkLoc = loc.clone().add(x, -0.1, z);
                Material mat = checkLoc.getBlock().getType();
                if (mat.isSolid() && mat != Material.AIR) {
                    return true;
                }
            }
        }

        // Alternative ground check without deprecated method
        Location below = loc.clone().subtract(0, 0.1, 0);
        return below.getBlock().getType().isSolid();
    }

    private boolean hasPerkEnabled(Player player) {
        PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);
        return perkData.getEnabledPerks().contains(this);
    }

    private boolean isOnCooldown(Player player) {
        Long lastUsage = cooldowns.get(player.getUniqueId());
        return lastUsage != null && (System.currentTimeMillis() - lastUsage) < COOLDOWN_MS;
    }

    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}