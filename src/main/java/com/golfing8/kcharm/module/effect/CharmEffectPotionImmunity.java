package com.golfing8.kcharm.module.effect;

import com.golfing8.kcharm.KCharms;
import com.google.common.base.Preconditions;
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
public class CharmEffectPotionImmunity extends CharmEffect {
    /**
     * Maps effect type to the amplifier and up that the player is immune to.
     * <p>
     * i.e. if this contains WEAKNESS: 1, the player is immune to weakness 2+, but weakness 1 will still be applied.
     * </p>
     */
    private final Map<PotionEffectType, Integer> potionEffectImmunities;

    public CharmEffectPotionImmunity(String id, ConfigurationSection section) {
        super(id, section);
        Preconditions.checkArgument(section.isConfigurationSection("immunities"), "Must contain a list `potion-effects`");

        potionEffectImmunities = new HashMap<>();
        ConfigurationSection immunitySection = section.getConfigurationSection("immunities");
        for (String effect : immunitySection.getKeys(false)) {
            PotionEffectType type = PotionEffectType.getByName(effect);
            if (type == null)
                throw new IllegalArgumentException("Potion effect %s doesn't exist.".formatted(effect));

            int amplifier = immunitySection.getInt(effect);
            potionEffectImmunities.put(type, amplifier);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEffectApply(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        if (event.getNewEffect() == null)
            return;

        if (!potionEffectImmunities.containsKey(event.getNewEffect().getType()))
            return;

        if (!isAffectedByCharm(player))
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
}
