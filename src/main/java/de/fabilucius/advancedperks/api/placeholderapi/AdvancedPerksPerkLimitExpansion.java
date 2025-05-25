package de.fabilucius.advancedperks.api.placeholderapi;

import com.google.inject.Inject;
import de.fabilucius.advancedperks.AdvancedPerks;
import de.fabilucius.advancedperks.core.configuration.type.SettingsConfiguration;
import de.fabilucius.advancedperks.data.PerkData;
import de.fabilucius.advancedperks.data.PerkDataRepository;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancedPerksPerkLimitExpansion extends AbstractAdvancedPerksExpansion {

    @Inject
    private PerkDataRepository perkDataRepository;

    @Inject
    private SettingsConfiguration settingsConfiguration;

    @Inject
    public AdvancedPerksPerkLimitExpansion(AdvancedPerks advancedPerks) {
        super(advancedPerks);
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        PerkData perkData = this.perkDataRepository.getPerkDataByPlayer(player);
        int maxPerksAmount = perkData.getEffectiveMaxPerks(this.settingsConfiguration.getGlobalPerkLimit());
        return String.valueOf(perkData.getEnabledPerks().size() >= maxPerksAmount);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "advancedperks.perk-limit";
    }
}