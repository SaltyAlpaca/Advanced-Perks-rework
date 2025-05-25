package de.fabilucius.advancedperks.api.placeholderapi;

import de.fabilucius.advancedperks.AdvancedPerks;
import de.fabilucius.advancedperks.api.AdvancedPerksAPI;
import de.fabilucius.advancedperks.registry.PerkRegistryImpl;
import de.fabilucius.advancedperks.perk.Perk;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Placeholder to check if a player has permission (unlock) for a perk.
 * Usage: %advancedperks.use_&lt;perkId&gt;%
 */
public class AdvancedPerksUseExpansion extends AbstractAdvancedPerksExpansion {
    private final PerkRegistryImpl perkRegistry;

    public AdvancedPerksUseExpansion(AdvancedPerks plugin, PerkRegistryImpl perkRegistry) {
        super(plugin);
        this.perkRegistry = perkRegistry;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "advancedperks.use";
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
        boolean unlocked = AdvancedPerksAPI.isPerkUnlocked(player, params);
        return String.valueOf(unlocked);
    }
}