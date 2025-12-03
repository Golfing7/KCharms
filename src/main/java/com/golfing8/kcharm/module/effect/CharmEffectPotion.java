package com.golfing8.kcharm.module.effect;

import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.google.common.base.Preconditions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * Gives players potion effects when holding a specific charm.
 */
public class CharmEffectPotion extends CharmEffect {
    private final List<PotionEffect> giveOnHold;

    public CharmEffectPotion(String id, ConfigurationSection section) {
        super(id, section);
        Preconditions.checkArgument(section.isList("potion-effects"), "Must contain a list `potion-effects`");

        this.giveOnHold = new ArrayList<>();
        for (String potEffect : section.getStringList("potion-effects")) {
            giveOnHold.add(ConfigTypeRegistry.getFromType(ConfigPrimitive.ofString(potEffect), PotionEffect.class));
        }
    }

    @Override
    public void startEffect(Player player) {
        for (PotionEffect effect : giveOnHold) {
            player.addPotionEffect(effect);
        }
    }

    @Override
    public void tickEffect(Player player) {
        if (NMS.getTheNMS().getCurrentTick() % 20 != 0)
            return;

        for (PotionEffect effect : giveOnHold) {
            player.addPotionEffect(effect);
        }
    }

    @Override
    public void stopEffect(Player player) {
        for (PotionEffect effect : giveOnHold) {
            player.removePotionEffect(effect.getType());
        }
    }
}
