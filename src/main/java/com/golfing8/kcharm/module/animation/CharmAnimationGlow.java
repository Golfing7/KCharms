package com.golfing8.kcharm.module.animation;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

/**
 * Uses the 'glowing' enchant to make players glow.
 */
public class CharmAnimationGlow extends CharmAnimation {
    public CharmAnimationGlow(ConfigurationSection section) {
        super(section);
    }

    @Override
    public void onActivate(Player holdingCharm, Set<Player> affectedPlayers) {
        for (Player player : affectedPlayers) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 25, 0));
        }
    }

    @Override
    public void onTick(Player holdingCharm, Set<Player> affectedPlayers) {
        for (Player player : affectedPlayers) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 25, 0), true);
        }
    }

    @Override
    public void onDeactivate(Player holdingCharm, Set<Player> affectedPlayers) {
        for (Player player : affectedPlayers) {
            player.removePotionEffect(PotionEffectType.GLOWING);
        }
    }
}
