package de.fabilucius.advancedperks.data.state;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import de.fabilucius.advancedperks.core.configuration.type.SettingsConfiguration;
import de.fabilucius.advancedperks.data.PerkData;
import de.fabilucius.advancedperks.data.PerkDataRepository;
import de.fabilucius.advancedperks.event.perk.PerkDisableEvent;
import de.fabilucius.advancedperks.event.perk.PerkEnableEvent;
import de.fabilucius.advancedperks.perk.Perk;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class PerkStateController {

    private final int globalMaxActivePerks;
    private final LuckPerms luckPerms;

    @Inject
    private PerkDataRepository perkDataRepository;

    @Inject
    public PerkStateController(SettingsConfiguration settingsConfiguration, LuckPerms luckPerms) {
        this.globalMaxActivePerks = settingsConfiguration.getGlobalPerkLimit();
        this.luckPerms = luckPerms;
    }

    private int getPermissionBasedMaxPerks(Player player) {
        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        return user.getNodes().stream()
                .filter(PermissionNode.class::isInstance)
                .mapToInt(node -> {
                    String permission = ((PermissionNode) node).getPermission();
                    String[] parts = permission.split("\\.");
                    try {
                        return Integer.parseInt(parts[parts.length - 1]);
                    } catch (NumberFormatException e) {
                        return -1; // Ignore invalid permissions.
                    }
                })
                .filter(maxPerks -> maxPerks > 0) // Only positive values
                .findFirst()
                .orElse(globalMaxActivePerks);
    }


    public PerkUseStatus canUsePerk(Player player, Perk perk) {
        PerkData perkData = this.perkDataRepository.getPerkDataByPlayer(player);

        /* Permission check */
        if (perk.getPermission().isPresent() && !player.hasPermission(perk.getPermission().get()) &&
                perkData.getBoughtPerks().stream().noneMatch(boughtPerk -> boughtPerk.equalsIgnoreCase(perk.getIdentifier()))) {
            return PerkUseStatus.NO_PERMISSION;
        }

        int maxActivePerksAllowed = Math.max(this.globalMaxActivePerks, getPermissionBasedMaxPerks(player));

        /* Max perk active check */
        if (perkData.getEnabledPerks().size() >= maxActivePerksAllowed && maxActivePerksAllowed >= 0) {
            return PerkUseStatus.TOO_MANY_ACTIVE;
        }

        /* Disallowed world check */
        if (perk.getDisallowedWorlds().isPresent() && perk.getDisallowedWorlds().get().stream().anyMatch(world -> world.equalsIgnoreCase(player.getWorld().getName()))) {
            return PerkUseStatus.DISALLOWED_WORLD;
        }

        return PerkUseStatus.CAN_BE_USED;
    }

    public void disableAllPerks(Player player) {
        PerkData perkData = this.perkDataRepository.getPerkDataByPlayer(player);
        Lists.newArrayList(perkData.getEnabledPerks()).forEach(perk -> this.disablePerk(player, perk));
    }

    public PerkToggleResult forceTogglePerk(Player player, Perk perk) {
        PerkData perkData = this.perkDataRepository.getPerkDataByPlayer(player);
        if (perkData.getEnabledPerks().contains(perk)) {
            return this.forceDisablePerk(player, perk);
        } else {
            return this.forceEnablePerk(player, perk);
        }
    }

    public PerkToggleResult togglePerk(Player player, Perk perk) {
        PerkData perkData = this.perkDataRepository.getPerkDataByPlayer(player);
        if (perkData.getEnabledPerks().contains(perk)) {
            return this.disablePerk(player, perk);
        } else {
            return this.enablePerk(player, perk);
        }
    }

    public PerkToggleResult forceEnablePerk(Player player, Perk perk) {
        PerkData perkData = this.perkDataRepository.getPerkDataByPlayer(player);
        PerkEnableEvent perkEnableEvent = new PerkEnableEvent(player, perk, true);
        Bukkit.getPluginManager().callEvent(perkEnableEvent);
        if (perkData.getEnabledPerks().add(perk)) {
            perk.onPrePerkEnable(player);
        }
        return PerkToggleResult.ENABLED;
    }

    public PerkToggleResult enablePerk(Player player, Perk perk) {
        PerkUseStatus perkUseStatus = this.canUsePerk(player, perk);
        switch (perkUseStatus) {
            case DISALLOWED_WORLD -> {
                return PerkToggleResult.DISALLOWED_WORLD;
            }
            case NO_PERMISSION -> {
                return PerkToggleResult.NO_PERMISSION;
            }
            case TOO_MANY_ACTIVE -> {
                return PerkToggleResult.TOO_MANY_ACTIVE;
            }
            default -> {
                PerkData perkData = this.perkDataRepository.getPerkDataByPlayer(player);
                PerkEnableEvent perkEnableEvent = new PerkEnableEvent(player, perk, false);
                Bukkit.getPluginManager().callEvent(perkEnableEvent);
                if (perkEnableEvent.isCancelled()) {
                    return PerkToggleResult.EVENT_CANCELLED;
                }
                if (perkData.getEnabledPerks().add(perk)) {
                    perk.onPrePerkEnable(player);
                }
                return PerkToggleResult.ENABLED;
            }
        }
    }

    public PerkToggleResult forceDisablePerk(Player player, Perk perk) {
        return this.disable(player, perk, true);
    }

    public PerkToggleResult disablePerk(Player player, Perk perk) {
        return this.disable(player, perk, false);
    }

    public PerkToggleResult disable(Player player, Perk perk, boolean force) {
        PerkData perkData = this.perkDataRepository.getPerkDataByPlayer(player);
        PerkDisableEvent perkDisableEvent = new PerkDisableEvent(player, perk, force);
        Bukkit.getPluginManager().callEvent(perkDisableEvent);
        if (perkDisableEvent.isCancelled() && !force) {
            return PerkToggleResult.EVENT_CANCELLED;
        }
        if (perkData.getEnabledPerks().remove(perk)) {
            perk.onPrePerkDisable(player);
        }
        return PerkToggleResult.DISABLED;
    }

    public void disablePerks(Player player, List<Perk> perks) {
        perks.forEach(perk -> this.disablePerk(player, perk));
    }

}
