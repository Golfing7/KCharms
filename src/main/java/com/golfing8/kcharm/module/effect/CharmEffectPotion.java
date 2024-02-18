package com.golfing8.kcharm.module.effect;

import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * Gives players potion effects when holding a specific charm.
 */
@AllArgsConstructor
public class CharmEffectPotion extends CharmEffect {
    private final List<PotionEffect> giveOnHold;

    @Override
    public void onStartHolding(Player player) {
        for (PotionEffect effect : giveOnHold) {
            player.addPotionEffect(effect);
        }
    }

    @Override
    public void onHolding(Player player) {
        if (NMS.getTheNMS().getCurrentTick() % 20 != 0)
            return;

        for (PotionEffect effect : giveOnHold) {
            player.addPotionEffect(effect);
        }
    }

    @Override
    public void onStopHolding(Player player) {
        for (PotionEffect effect : giveOnHold) {
            player.removePotionEffect(effect.getType());
        }
    }

    /**
     * Loads an instance from the given config section.
     *
     * @param section the section.
     * @return the potion effect.
     */
    public static CharmEffectPotion fromConfig(ConfigurationSection section) {
        Preconditions.checkArgument(section.isList("potion-effects"), "Must contain a list `potion-effects`");

        List<PotionEffect> allEffects = new ArrayList<>();
        for (String potEffect : section.getStringList("potion-effects")) {
            allEffects.add(ConfigTypeRegistry.getFromType(ConfigPrimitive.ofString(potEffect), PotionEffect.class));
        }
        return new CharmEffectPotion(allEffects);
    }
}
