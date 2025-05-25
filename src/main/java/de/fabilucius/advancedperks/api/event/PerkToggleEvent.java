package de.fabilucius.advancedperks.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a perk is toggled (enabled/disabled) for a player via the API or plugin logic.
 */
public class PerkToggleEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String perkId;
    private final boolean enabled;

    public PerkToggleEvent(Player player, String perkId, boolean enabled) {
        this.player = player;
        this.perkId = perkId;
        this.enabled = enabled;
    }

    public Player getPlayer() {
        return player;
    }

    public String getPerkId() {
        return perkId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}