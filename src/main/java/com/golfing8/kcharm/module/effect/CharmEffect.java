package com.golfing8.kcharm.module.effect;

import org.bukkit.entity.Player;

/**
 * Defines an effect that a charm has.
 * <p>
 * One charm can have multiple effects 'compounded' together.
 * </p>
 */
public abstract class CharmEffect {
    public abstract void onStartHolding(Player player);

    /**
     *
     * @param player
     */
    public abstract void onHolding(Player player);

    /**
     * Called when a player has stopped holding a charm that has this effect.
     * <p>
     * At this point the charm is still in the player's hand, but about to be removed.
     * </p>
     *
     * @param player the player.
     */
    public abstract void onStopHolding(Player player);
}
