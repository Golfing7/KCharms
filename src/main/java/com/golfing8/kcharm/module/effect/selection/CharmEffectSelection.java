package com.golfing8.kcharm.module.effect.selection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.function.BiPredicate;

/**
 * Useful for deciding who a charm's effect selects.
 */
@Getter
@AllArgsConstructor
public enum CharmEffectSelection {
    SELF,
    ENEMY,
    TEAM,
}
