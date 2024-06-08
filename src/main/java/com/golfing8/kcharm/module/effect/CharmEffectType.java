package com.golfing8.kcharm.module.effect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Function;

/**
 * Contains all types of charms.
 */
@AllArgsConstructor
public enum CharmEffectType {
    ATTRIBUTE(CharmEffectAttribute::new),
    DAMAGE_RESISTANCE(CharmEffectDamageResistance::new),
    POTION(CharmEffectPotion::new),
    POTION_IMMUNITY(CharmEffectPotionImmunity::new),
    ;

    /** Maps a config section to a loaded instance of a charm effect */
    @Getter
    final Function<ConfigurationSection, CharmEffect> configLoader;

    /**
     * Loads a charm effect from its given section.
     *
     * @param section the section.
     * @return the effect.
     */
    public static CharmEffect fromConfig(ConfigurationSection section) {
        if (!section.isString("type"))
            throw new IllegalArgumentException("Type must be string. Was " + section.getString("type"));

        CharmEffectType type;
        try {
            type = CharmEffectType.valueOf(section.getString("type"));
        } catch (IllegalArgumentException exc) {
            throw new IllegalArgumentException("Cannot load charm effect because '%s' type doesn't exist!".formatted(section.getString("type")));
        }

        return type.getConfigLoader().apply(section);
    }
}
