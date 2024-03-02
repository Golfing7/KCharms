package com.golfing8.kcharm.module.animation;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Represents an animation that a charm can use.
 * <p>
 * Animations are ranges of visual/audio effects that add to immersion
 * and can indicate to enemies/allies of an active buff.
 * </p>
 */
public abstract class CharmAnimation {
    public CharmAnimation(ConfigurationSection section) {}

    /**
     * Called when an active-style charm is activated.
     * This is NEVER called for passive charm effects.
     *
     * @param holdingCharm the player holding the charm.
     * @param affectedPlayers the players the charm will affect.
     */
    public void onActivate(Player holdingCharm, Set<Player> affectedPlayers) {}

    /**
     * Called every second while someone is holding a charm.
     *
     * @param holdingCharm the player holding the charm.
     * @param affectedPlayers the players under the affect of the charm.
     */
    public void onTick(Player holdingCharm, Set<Player> affectedPlayers) {}

    /**
     * Called when an active-style charm is deactivated (effective duration ran out).
     * This is NEVER called for passive charm effects.
     *
     * @param holdingCharm the player holding the charm.
     * @param affectedPlayers the players under the affect of the charm.
     */
    public void onDeactivate(Player holdingCharm, Set<Player> affectedPlayers) {}

    /**
     * Called when a player is no longer being affected by a charm's effect.
     * This is used as players may quit the server or in some way leave the effective range of the charm's effect.
     *
     * @param player the player.
     */
    public void stopEffect(Player player) {}
}
