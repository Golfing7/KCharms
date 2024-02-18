package com.golfing8.kcharm.module.effect;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Function;

/**
 * Contains all types of charms.
 */
@AllArgsConstructor
public enum CharmEffectType {
    ATTRIBUTE(CharmEffectAttribute::fromConfig),
    DAMAGE_RESISTANCE(CharmEffectDamageResistance::fromConfig),
    POTION(CharmEffectPotion::fromConfig),
    POTION_IMMUNITY(CharmEffectPotionImmunity::fromConfig),
    ;

    /** Maps a config section to a loaded instance of a charm effect */
    @Getter
    Function<ConfigurationSection, CharmEffect> configLoader;

    /**
     * Loads a charm effect from its given section.
     *
     * @param section the section.
     * @return the effect.
     */
    public static CharmEffect fromConfig(ConfigurationSection section) {
        Preconditions.checkArgument(section.isString("type"));

        CharmEffectType type;
        try {
            type = CharmEffectType.valueOf(section.getString("type"));
        } catch (IllegalArgumentException exc) {
            throw new IllegalArgumentException("Cannot load charm effect because '%s' type doesn't exist!".formatted(section.getString("type")));
        }

        return type.getConfigLoader().apply(section);
    }
}
