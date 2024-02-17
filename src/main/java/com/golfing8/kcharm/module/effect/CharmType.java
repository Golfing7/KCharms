package com.golfing8.kcharm.module.effect;

import com.golfing8.kcharm.module.CharmModule;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Function;

/**
 * Contains all types of charms.
 */
@AllArgsConstructor
public enum CharmType {
    POTION(CharmPotionEffect::fromConfig),
    POTION_IMMUNITY(CharmPotionEffectImmunity::fromConfig),
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

        CharmType type;
        try {
            type = CharmType.valueOf(section.getString("type"));
        } catch (IllegalArgumentException exc) {
            throw new IllegalArgumentException("Cannot load charm effect because '%s' type doesn't exist!".formatted(section.getString("type")));
        }

        return type.getConfigLoader().apply(section);
    }
}
