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
 * Sets players on fire.
 */
public class CharmEffectSetFire extends CharmEffect {
    private final int fireTicks;

    public CharmEffectSetFire(ConfigurationSection section) {
        super(section);
        Preconditions.checkArgument(section.isInt("fire-ticks"), "Must contain an int `fire-ticks`");

        this.fireTicks = section.getInt("fire-ticks", 200);
    }

    @Override
    public void startEffect(Player player) {
        player.setFireTicks(player.getFireTicks() + fireTicks);
    }

    @Override
    public void tickEffect(Player player) {
        if (NMS.getTheNMS().getCurrentTick() % 20 != 0)
            return;

        // re-adjust fire ticks again
        player.setFireTicks(player.getFireTicks() + 20);
    }

    @Override
    public void stopEffect(Player player) {
        player.setFireTicks(Math.max(0, player.getFireTicks() - fireTicks));
    }
}
