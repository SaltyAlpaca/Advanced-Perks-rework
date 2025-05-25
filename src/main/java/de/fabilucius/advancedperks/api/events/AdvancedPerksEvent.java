package de.fabilucius.advancedperks.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base class for all AdvancedPerks API events
 */
public abstract class AdvancedPerksEvent extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String perkId;
    
    public AdvancedPerksEvent(Player player, String perkId) {
        this.player = player;
        this.perkId = perkId;
    }
    
    public AdvancedPerksEvent(Player player, String perkId, boolean async) {
        super(async);
        this.player = player;
        this.perkId = perkId;
    }
    
    /**
     * Gets the player involved in this event
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Gets the perk identifier
     * @return The perk ID
     */
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

/**
 * Called when a perk is enabled for a player
 */
class PerkEnabledEvent extends AdvancedPerksEvent implements Cancellable {
    
    private boolean cancelled;
    private final boolean forced;
    
    public PerkEnabledEvent(Player player, String perkId, boolean forced) {
        super(player, perkId);
        this.forced = forced;
    }
    
    /**
     * Checks if this enable was forced (bypassed checks)
     * @return true if forced
     */
    public boolean isForced() {
        return forced;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}

/**
 * Called when a perk is disabled for a player
 */
class PerkDisabledEvent extends AdvancedPerksEvent implements Cancellable {
    
    private boolean cancelled;
    private final boolean forced;
    
    public PerkDisabledEvent(Player player, String perkId, boolean forced) {
        super(player, perkId);
        this.forced = forced;
    }
    
    /**
     * Checks if this disable was forced
     * @return true if forced
     */
    public boolean isForced() {
        return forced;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}

/**
 * Called when a perk is purchased by a player
 */
class PerkPurchasedEvent extends AdvancedPerksEvent {
    
    private final double price;
    
    public PerkPurchasedEvent(Player player, String perkId, double price) {
        super(player, perkId);
        this.price = price;
    }
    
    /**
     * Gets the price paid for the perk
     * @return The price
     */
    public double getPrice() {
        return price;
    }
}

/**
 * Called when a player's perk data is loaded
 */
class PlayerDataLoadedEvent extends Event {
    
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    
    public PlayerDataLoadedEvent(Player player) {
        super(true); // Async
        this.player = player;
    }
    
    /**
     * Gets the player whose data was loaded
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}