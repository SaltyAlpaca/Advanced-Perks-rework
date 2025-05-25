package de.fabilucius.advancedperks.api.placeholderapi;

import com.google.inject.Inject;
import de.fabilucius.advancedperks.AdvancedPerks;
import de.fabilucius.advancedperks.api.AdvancedPerksAPI;
import de.fabilucius.advancedperks.perk.Perk;
import de.fabilucius.advancedperks.registry.PerkRegistryImpl;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Placeholder to check if a perk is currently enabled for a player.
 * Usage: %advancedperks.enabled_&lt;perkId&gt;%
 */
public class AdvancedPerksEnabledExpansion extends AbstractAdvancedPerksExpansion {
    private final PerkRegistryImpl perkRegistry;

    @Inject
    public AdvancedPerksEnabledExpansion(AdvancedPerks plugin, PerkRegistryImpl perkRegistry) {
        super(plugin);
        this.perkRegistry = perkRegistry;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "advancedperks.enabled";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null || params.isEmpty()) {
            return "";
        }
        Perk perk = perkRegistry.getPerkByIdentifier(params);
        if (perk == null) {
            return "";
        }
        boolean enabled = AdvancedPerksAPI.isPerkEnabled(player, params);
        return String.valueOf(enabled);
    }
}