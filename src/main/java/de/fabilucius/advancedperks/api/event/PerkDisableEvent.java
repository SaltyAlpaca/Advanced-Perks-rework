package de.fabilucius.advancedperks.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a perk is disabled for a player via the API or plugin logic.
 */
public class PerkDisableEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String perkId;

    public PerkDisableEvent(Player player, String perkId) {
        this.player = player;
        this.perkId = perkId;
    }

    public Player getPlayer() {
        return player;
    }

    public String getPerkId() {
        return perkId;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
