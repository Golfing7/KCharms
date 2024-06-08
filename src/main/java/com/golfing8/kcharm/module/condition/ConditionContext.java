package com.golfing8.kcharm.module.condition;

import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Context used for checking a condition for a charm.
 *
 * @param holder the holder of the charm.
 * @param applicablePlayers the players that the charm applies to.
 * @param startedHoldingTick the tick that the player started holding the charm.
 */
public record ConditionContext(Player holder,
                               Set<Player> applicablePlayers,
                               int startedHoldingTick) {}
