package de.fabilucius.advancedperks.perk.defaultperks.listener;

import com.google.inject.Inject;
import de.fabilucius.advancedperks.AdvancedPerks;
import de.fabilucius.advancedperks.data.PerkData;
import de.fabilucius.advancedperks.data.PerkDataRepository;
import de.fabilucius.advancedperks.perk.Perk;
import de.fabilucius.advancedperks.perk.AbstractDefaultPerk;
import de.fabilucius.advancedperks.perk.annotation.PerkIdentifier;
import de.fabilucius.advancedperks.perk.properties.PerkDescription;
import de.fabilucius.advancedperks.perk.properties.PerkGuiIcon;
import de.fabilucius.advancedperks.perk.types.ListenerPerk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@PerkIdentifier("double_jump")
public class DoubleJumpPerk extends AbstractDefaultPerk implements ListenerPerk {

    private static final double JUMP_POWER = 0.9;
    private static final double FORWARD_MULTIPLIER = 0.4;
    private static final double GROUND_CHECK_DISTANCE = 0.3;
    @Inject
    private PerkDataRepository perkDataRepository;
    @Inject
    private AdvancedPerks advancedPerks;

    private final Map<UUID, Boolean> canDoubleJump = new HashMap<>();
    private final Map<UUID, Boolean> wasOnGround = new HashMap<>();

    public DoubleJumpPerk(String identifier, String displayName, PerkDescription perkDescription,
                          PerkGuiIcon perkGuiIcon, boolean enabled, Map<String, Object> flags) {
        super(identifier, displayName, perkDescription, perkGuiIcon, enabled, flags);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (hasPerkEnabled(player)) {
            enableDoubleJump(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        canDoubleJump.remove(uuid);
        wasOnGround.remove(uuid);
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        advancedPerks.getServer().getScheduler().runTaskLater(advancedPerks, () -> {
            if (hasPerkEnabled(player)) {
                enableDoubleJump(player);
                canDoubleJump.put(uuid, true);
                wasOnGround.put(uuid, true);
            } else {
                disableDoubleJump(player);
                canDoubleJump.remove(uuid);
                wasOnGround.remove(uuid);
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!hasPerkEnabled(player)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        boolean currentlyOnGround = isPlayerOnGround(player);
        boolean previouslyOnGround = Boolean.TRUE.equals(wasOnGround.get(uuid));

        wasOnGround.put(uuid, currentlyOnGround);

        if (currentlyOnGround && !previouslyOnGround) {
            canDoubleJump.put(uuid, true);
            // allowFlight aktivieren wenn Spieler landet (für nächsten möglichen Double Jump)
            if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                player.setAllowFlight(true);
                player.setFlying(false);
            }
        }

        if (currentlyOnGround && player.isFlying()) {
            player.setFlying(false);
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (!hasPerkEnabled(player)) {
            return;
        }

        event.setCancelled(true);

        UUID uuid = player.getUniqueId();

        if (!Boolean.TRUE.equals(canDoubleJump.get(uuid))) {
            return;
        }

        performDoubleJump(player);
        canDoubleJump.put(uuid, false);

        // allowFlight AUS nach Jump (damit Fallschaden möglich ist)
        player.setAllowFlight(false);
        player.setFlying(false);
    }


    @Override
    public void onPerkEnable(Player player) {
        disableBirdPerk(player);
        UUID uuid = player.getUniqueId();
        canDoubleJump.put(uuid, true);
        wasOnGround.put(uuid, isPlayerOnGround(player));
        // KEIN automatisches allowFlight mehr!
    }

    @Override
    public void onPerkDisable(Player player) {
        disableDoubleJump(player);
        UUID uuid = player.getUniqueId();
        canDoubleJump.remove(uuid);
        wasOnGround.remove(uuid);
    }

    private void performDoubleJump(Player player) {
        Vector velocity = player.getVelocity();
        Vector direction = player.getLocation().getDirection();

        velocity.setY(JUMP_POWER);
        velocity.add(direction.multiply(FORWARD_MULTIPLIER));

        player.setVelocity(velocity);

        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.4f, 1.5f);
        player.getWorld().spawnParticle(Particle.CLOUD,
                player.getLocation().add(0, 0.3, 0),
                8, 0.2, 0.1, 0.2, 0.05);
    }

    private void enableDoubleJump(Player player) {
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setAllowFlight(true);
            player.setFlying(false);
        }
    }

    private void disableDoubleJump(Player player) {
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    private boolean isPlayerOnGround(Player player) {
        Location loc = player.getLocation();
        for (double y = 0.1; y <= GROUND_CHECK_DISTANCE; y += 0.1) {
            Location below = loc.clone().subtract(0, y, 0);
            if (below.getBlock().getType().isSolid()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPerkEnabled(Player player) {
        PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);
        return perkData.getEnabledPerks().contains(this);
    }

    private void disableBirdPerk(Player player) {
        PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);

        Perk birdPerk = null;
        for (Perk perk : perkData.getEnabledPerks()) {
            if (perk.getIdentifier().equals("bird")) {
                birdPerk = perk;
                break;
            }
        }

        if (birdPerk != null) {
            perkData.getEnabledPerks().remove(birdPerk);
            perkDataRepository.savePerkDataAsync(perkData);

            if (birdPerk instanceof AbstractDefaultPerk) {
                birdPerk.onPerkDisable(player);
            }
        }
    }
}