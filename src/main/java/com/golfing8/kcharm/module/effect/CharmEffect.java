package com.golfing8.kcharm.module.effect;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Defines an effect that a charm has.
 * <p>
 * One charm can have multiple effects 'compounded' together.
 * </p>
 */
public abstract class CharmEffect implements Listener {
    /**
     * Called when a player has begun holding this charm's effect.
     *
     * @param player the player.
     */
    public void onStartHolding(Player player) {}

    /**
     * Called every tick WHILE a player is holding the charm.
     *
     * @param player the player.
     */
    public void onHolding(Player player) {}

    /**
     * Called when a player has stopped holding a charm that has this effect.
     * <p>
     * At this point the charm is still in the player's hand, but about to be removed.
     * </p>
     *
     * @param player the player.
     */
    public void onStopHolding(Player player) {}

    /**
     * Checks if the given player is holding a charm with this effect.
     *
     * @param player the player.
     * @return if they're holding this charm.
     */
    protected boolean isHoldingCharm(Player player) {
        //TODO
        return false;
    }
}
