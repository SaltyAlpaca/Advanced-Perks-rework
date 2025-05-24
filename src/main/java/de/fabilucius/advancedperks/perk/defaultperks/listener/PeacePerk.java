package de.fabilucius.advancedperks.perk.defaultperks.listener;

import com.google.inject.Inject;
import de.fabilucius.advancedperks.data.PerkData;
import de.fabilucius.advancedperks.data.PerkDataRepository;
import de.fabilucius.advancedperks.perk.AbstractDefaultPerk;
import de.fabilucius.advancedperks.perk.annotation.PerkIdentifier;
import de.fabilucius.advancedperks.perk.properties.PerkDescription;
import de.fabilucius.advancedperks.perk.properties.PerkGuiIcon;
import de.fabilucius.advancedperks.perk.types.ListenerPerk;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.Location;

import java.util.Map;

@PerkIdentifier("peace")
public class PeacePerk extends AbstractDefaultPerk implements ListenerPerk {

    @Inject
    private PerkDataRepository perkDataRepository;

    public PeacePerk(String identifier, String displayName, PerkDescription perkDescription, PerkGuiIcon perkGuiIcon, boolean enabled, Map<String, Object> flags) {
        super(identifier, displayName, perkDescription, perkGuiIcon, enabled, flags);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);

            // Check if the PeacePerk is active and the entity is a mob
            if (perkData.getEnabledPerks().contains(this) && event.getEntity() instanceof Mob) {
                if (player.getWorld().getName().contains("Dungeon")) {
                    return; // Exit without cancelling if it's a dungeon world
                }
                event.setCancelled(true); // Mobs ignore the player
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // When a player attacks a mob, nearby mobs of the same type become aggressive
        if (event.getDamager() instanceof Player player && event.getEntity() instanceof Mob mob) {
            PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);

            if (perkData.getEnabledPerks().contains(this)) {
                // Set the mob that was attacked to be aggressive towards the player
                mob.setTarget(player);

                // Get the class type of the mob that was attacked
                Class<?> mobType = mob.getClass();

                // Make all mobs of the same type within a 15-block radius aggressive towards the player
                Location playerLocation = player.getLocation();
                player.getWorld().getNearbyEntities(playerLocation, 15, 15, 15)
                        .stream()
                        .filter(mobType::isInstance) // Only include mobs of the same type
                        .map(Mob.class::cast) // Use method reference to cast to Mob
                        .forEach(nearbyMob -> nearbyMob.setTarget(player));
            }
        }
    }


}
