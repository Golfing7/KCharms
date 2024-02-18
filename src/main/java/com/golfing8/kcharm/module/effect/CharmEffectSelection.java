package com.golfing8.kcharm.module.effect;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Useful for deciding who a charm's effect selects.
 */
@AllArgsConstructor
public enum CharmEffectSelection {
    SELF((player, range) -> Collections.singletonList(player)),
    ENEMY((player, range) -> {
        return player.getNearbyEntities(range, range, range).stream().filter(other -> other instanceof Player).map(e -> (Player) e).collect(Collectors.toList());
    }),
    TEAM((player, range) -> {
        return player.getNearbyEntities(range, range, range).stream().filter(other -> other instanceof Player).map(e -> (Player) e).collect(Collectors.toList());
    }),
    ;

    BiFunction<Player, Double, List<Player>> playerGetter;
}
