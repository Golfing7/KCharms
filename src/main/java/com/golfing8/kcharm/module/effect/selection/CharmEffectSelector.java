package com.golfing8.kcharm.module.effect.selection;

import org.bukkit.entity.Player;

/**
 * Abstract functionality for selecting players.
 */
public interface CharmEffectSelector {
    /**
     * Returns true if the player is itself.
     *
     * @param player the player.
     * @param other the other player.
     * @return true if the player is itself.
     */
    default boolean isSelf(Player player, Player other) {
        return player == other;
    }

    /**
     * Returns true if the two players are allies.
     *
     * @param player the player
     * @param other the other player
     * @return true if the players are allies
     */
    default boolean isAlly(Player player, Player other) {
        return player == other;
    }

    /**
     * Returns true if the players are enemies
     *
     * @param player the player
     * @param other the other player
     * @return true if the players are enemies
     */
    default boolean isEnemy(Player player, Player other) {
        return player != other;
    }
}
