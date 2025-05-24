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
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@PerkIdentifier("double_jump")
public class DoubleJumpPerk extends AbstractDefaultPerk implements ListenerPerk, Listener {

    private static final long COOLDOWN_TICKS = 60;
    private static final double JUMP_POWER = 1.0;
    private static final double FORWARD_MULTIPLIER = 0.7;

    @Inject
    private PerkDataRepository perkDataRepository;
    @Inject
    private AdvancedPerks advancedPerks;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public DoubleJumpPerk(String identifier, String displayName, PerkDescription perkDescription,
                          PerkGuiIcon perkGuiIcon, boolean enabled, Map<String, Object> flags) {
        super(identifier, displayName, perkDescription, perkGuiIcon, enabled, flags);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updatePlayerFlightPermission(event.getPlayer());
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);
        if (!perkData.getEnabledPerks().contains(this)) {
            return;
        }

        event.setCancelled(true);
        player.setFlying(false);
        player.setAllowFlight(false);

        if (isOnCooldown(player)) {
            player.sendMessage("§cDer Double Jump hat noch Cooldown!");
            return;
        }

        performDoubleJump(player);
        setCooldown(player);

        advancedPerks.getServer().getScheduler().runTaskLater(advancedPerks,
                () -> updatePlayerFlightPermission(player), 1L);
    }

    @EventHandler
    public void onPlayerLand(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (isPlayerOnGround(player)) {
            updatePlayerFlightPermission(player);
        }
    }

    private void performDoubleJump(Player player) {
        Vector velocity = player.getLocation().getDirection().multiply(FORWARD_MULTIPLIER);
        velocity.setY(JUMP_POWER);
        player.setVelocity(velocity);

        // Visuelle und Audio-Effekte
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1f);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 10, 0.5, 0, 0.5, 0.1);
    }

    private void updatePlayerFlightPermission(Player player) {
        if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
            PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);
            if (perkData.getEnabledPerks().contains(this) && isPlayerOnGround(player)) {
                player.setAllowFlight(true);
                player.setFlying(false);
            }
        }
    }
    private boolean isPlayerOnGround(Player player) {
        return player.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid();
    }

    private boolean isOnCooldown(Player player) {
        Long lastUsage = cooldowns.get(player.getUniqueId());
        return lastUsage != null && (System.currentTimeMillis() - lastUsage) < (COOLDOWN_TICKS * 50);
    }

    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }


}