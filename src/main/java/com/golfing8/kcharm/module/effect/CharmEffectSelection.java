package com.golfing8.kcharm.module.effect;

import com.kamikazejam.factionintegrations.FactionIntegrations;
import com.kamikazejam.factionintegrations.object.TranslatedRelation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Useful for deciding who a charm's effect selects.
 */
@Getter
@AllArgsConstructor
public enum CharmEffectSelection {
    SELF((player, other) -> player == other),
    ENEMY((player, other) -> {
        boolean wilderness = !FactionIntegrations.getIntegration().hasFaction(player);
        if (wilderness)
            return player != other;

        return FactionIntegrations.getIntegration().getRelationToPlayer(player, other).isLessThan(TranslatedRelation.TRUCE);
    }),
    TEAM((player, other) -> {
        boolean wilderness = !FactionIntegrations.getIntegration().hasFaction(player);
        if (wilderness)
            return false;

        return FactionIntegrations.getIntegration().getRelationToPlayer(player, other).isGreaterThan(TranslatedRelation.NEUTRAL);
    }),
    ;

    BiPredicate<Player, Player> applicablePredicate;
}
