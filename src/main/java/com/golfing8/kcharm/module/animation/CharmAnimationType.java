package com.golfing8.kcharm.module.animation;

import com.golfing8.kcharm.module.effect.CharmEffect;
import com.golfing8.kcharm.module.effect.CharmEffectType;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Function;

/**
 * A registry enum for all charm animation types.
 */
@AllArgsConstructor
public enum CharmAnimationType {
    MESSAGE(CharmAnimationMessage::new),
    GLOW(CharmAnimationGlow::new),
    TEXT_DISPLAY(CharmAnimationTextDisplay::new),
    BLOCK_DISPLAY(CharmAnimationBlockDisplay::new),
    ITEM_DISPLAY(CharmAnimationItemDisplay::new),
    ;

    /** Maps a config section to a loaded instance of a charm animation */
    @Getter
    final Function<ConfigurationSection, CharmAnimation> configLoader;

    /**
     * Loads a charm animation from its given section.
     *
     * @param section the section.
     * @return the effect.
     */
    public static CharmAnimation fromConfig(ConfigurationSection section) {
        Preconditions.checkArgument(section.isString("type"));

        CharmAnimationType type;
        try {
            type = CharmAnimationType.valueOf(section.getString("type"));
        } catch (IllegalArgumentException exc) {
            throw new IllegalArgumentException("Cannot load charm effect because '%s' type doesn't exist!".formatted(section.getString("type")));
        }

        return type.getConfigLoader().apply(section);
    }
}
