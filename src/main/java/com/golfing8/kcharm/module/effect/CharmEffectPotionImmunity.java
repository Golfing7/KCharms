package com.golfing8.kcharm.module.effect;

import com.golfing8.kcharm.KCharms;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Gives you immunities to certain potion effects.
 */
@AllArgsConstructor
public class CharmEffectPotionImmunity extends CharmEffect {
    /**
     * Maps effect type to the amplifier and up that the player is immune to.
     * <p>
     * i.e. if this contains WEAKNESS: 1, the player is immune to weakness 2+, but weakness 1 will still be applied.
     * </p>
     */
    private Map<PotionEffectType, Integer> potionEffectImmunities;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEffectApply(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        if (event.getNewEffect() == null)
            return;

        if (!potionEffectImmunities.containsKey(event.getNewEffect().getType()))
            return;

        int amplifierResistant = potionEffectImmunities.get(event.getNewEffect().getType());
        if (amplifierResistant == 0) {
            event.setCancelled(true);
            return;
        }

        if (event.getNewEffect().getAmplifier() >= amplifierResistant) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(KCharms.getInstance(), () -> {
                player.addPotionEffect(event.getNewEffect().withAmplifier(amplifierResistant - 1));
            });
        }
    }

    /**
     * Loads an instance from the given config section.
     *
     * @param section the section.
     * @return the potion effect.
     */
    public static CharmEffectPotionImmunity fromConfig(ConfigurationSection section) {
        Preconditions.checkArgument(section.isConfigurationSection("immunities"), "Must contain a list `potion-effects`");

        Map<PotionEffectType, Integer> immunities = new HashMap<>();
        ConfigurationSection immunitySection = section.getConfigurationSection("immunities");
        for (String effect : immunitySection.getKeys(false)) {
            PotionEffectType type = PotionEffectType.getByName(effect);
            if (type == null)
                throw new IllegalArgumentException("Potion effect %s doesn't exist.".formatted(effect));

            int amplifier = immunitySection.getInt(effect);
            immunities.put(type, amplifier);
        }
        return new CharmEffectPotionImmunity(immunities);
    }
}
