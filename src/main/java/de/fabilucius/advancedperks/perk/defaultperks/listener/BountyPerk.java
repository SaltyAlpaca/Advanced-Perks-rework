package de.fabilucius.advancedperks.perk.defaultperks.listener;

import com.google.inject.Inject;
import de.fabilucius.advancedperks.data.PerkData;
import de.fabilucius.advancedperks.data.PerkDataRepository;
import de.fabilucius.advancedperks.perk.AbstractDefaultPerk;
import de.fabilucius.advancedperks.perk.annotation.PerkIdentifier;
import de.fabilucius.advancedperks.perk.properties.PerkDescription;
import de.fabilucius.advancedperks.perk.properties.PerkGuiIcon;
import de.fabilucius.advancedperks.perk.types.ListenerPerk;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;

@PerkIdentifier("bounty")
public class BountyPerk extends AbstractDefaultPerk implements ListenerPerk {

    @Inject
    private PerkDataRepository perkDataRepository;

    private final Random random = new Random();

    public BountyPerk(String identifier, String displayName, PerkDescription perkDescription, PerkGuiIcon perkGuiIcon, boolean enabled, Map<String, Object> flags) {
        super(identifier, displayName, perkDescription, perkGuiIcon, enabled, flags);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }

        Player player = event.getEntity().getKiller();
        PerkData perkData = perkDataRepository.getPerkDataByPlayer(player);

        if (perkData.getEnabledPerks().contains(this) && event.getEntityType() != EntityType.PLAYER) {
            for (ItemStack drop : event.getDrops()) {
                if (random.nextDouble() < 0.5) { // 25% Chance auf doppelten Drop
                    event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), drop.clone());
                }
            }
        }
    }
}
