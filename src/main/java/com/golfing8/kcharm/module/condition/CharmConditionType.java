package com.golfing8.kcharm.module.condition;

import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Function;

/**
 * A registry for charm conditions.
 */
@AllArgsConstructor
public enum CharmConditionType {
    AFFECTED_PLAYERS(CharmConditionAffectedPlayers::new),
    HELD_LENGTH(CharmConditionHeldLength::new),
    IN_REGION(CharmConditionInRegion::new),
    ;

    final Function<ConfigurationSection, CharmCondition> constructor;

    public static CharmCondition fromConfig(ConfigurationSection section) {
        if (!section.isString("type"))
            throw new IllegalArgumentException("Config section " + section.getCurrentPath() + " must have 'type'!");

        try {
            return valueOf(section.getString("type").toUpperCase()).constructor.apply(section);
        } catch (IllegalArgumentException exc) {
            throw new RuntimeException("Can't find charm condition of type " + section.getString("type"));
        }
    }
}
