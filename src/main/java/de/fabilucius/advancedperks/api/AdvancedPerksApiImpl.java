package de.fabilucius.advancedperks.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.fabilucius.advancedperks.data.state.PerkStateController;
import de.fabilucius.advancedperks.data.state.PerkToggleResult;
import de.fabilucius.advancedperks.data.state.PerkUseStatus;
import de.fabilucius.advancedperks.perk.Perk;
import de.fabilucius.advancedperks.registry.PerkRegistry;
import org.bukkit.entity.Player;

@Singleton
public class AdvancedPerksApiImpl implements AdvancedPerksSTATE {
    private final PerkStateController perkStateController;
    private final PerkRegistry perkRegistry;

    @Inject
    public AdvancedPerksApiImpl(PerkStateController perkStateController, PerkRegistry perkRegistry) {
        this.perkStateController = perkStateController;
        this.perkRegistry = perkRegistry;
    }

    @Override
    public PerkStateController getPerkStateController() {
        return this.perkStateController;
    }

    @Override
    public PerkRegistry getPerkRegistry() {
        return this.perkRegistry;
    }

    @Override
    public PerkToggleResult enablePerk(Player player, Perk perk) {
        return this.perkStateController.enablePerk(player, perk);
    }

    @Override
    public PerkToggleResult forceEnablePerk(Player player, Perk perk) {
        return this.perkStateController.forceEnablePerk(player, perk);
    }

    @Override
    public PerkToggleResult disablePerk(Player player, Perk perk) {
        return this.perkStateController.disablePerk(player, perk);
    }

    @Override
    public boolean hasPermissionForPerk(Player player, Perk perk) {
        PerkUseStatus useStatus = this.perkStateController.canUsePerk(player, perk);
        return useStatus != PerkUseStatus.NO_PERMISSION;
    }
}
