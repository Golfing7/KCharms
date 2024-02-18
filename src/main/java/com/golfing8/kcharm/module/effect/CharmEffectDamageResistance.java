package com.golfing8.kcharm.module.effect;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Modifies incoming damage for a player holding a charm.
 */
@AllArgsConstructor
public class CharmEffectDamageResistance extends CharmEffect {
    /** The damage causes that will receive the modified damage. */
    private final Set<EntityDamageEvent.DamageCause> damageCauses;
    /** The modifier of damage */
    private final double damageModifier;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;

        if (!damageCauses.isEmpty() && !damageCauses.contains(event.getCause()))
            return;

        event.setDamage(event.getDamage() * damageModifier);
    }

    public static CharmEffectDamageResistance fromConfig(ConfigurationSection section) {
        Preconditions.checkArgument(section.isDouble("damage-modifier"), "Must contain 'damage-modifier'");

        double damageMod = section.getDouble("damage-modifier");
        Set<EntityDamageEvent.DamageCause> causes = new HashSet<>();
        if (section.isList("causes")) {
            for (String str : section.getStringList("causes")) {
                causes.add(EntityDamageEvent.DamageCause.valueOf(str));
            }
        }

        return new CharmEffectDamageResistance(causes, damageMod);
    }
}
