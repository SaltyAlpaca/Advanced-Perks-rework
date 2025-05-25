package de.fabilucius.advancedperks.api.placeholderapi;

import de.fabilucius.advancedperks.AdvancedPerks;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

/**
 * Base PlaceholderAPI expansion for AdvancedPerks.
 */
public abstract class AbstractAdvancedPerksExpansion extends PlaceholderExpansion {
    protected final AdvancedPerks plugin;

    public AbstractAdvancedPerksExpansion(AdvancedPerks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
}