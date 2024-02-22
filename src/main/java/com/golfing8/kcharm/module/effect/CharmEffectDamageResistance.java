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
public class CharmEffectDamageResistance extends CharmEffect {
    /** The damage causes that will receive the modified damage. */
    private final Set<EntityDamageEvent.DamageCause> damageCauses;
    /** The modifier of damage */
    private final double damageModifier;

    public CharmEffectDamageResistance(ConfigurationSection section) {
        super(section);
        Preconditions.checkArgument(section.isDouble("damage-modifier"), "Must contain 'damage-modifier'");

        this.damageModifier = section.getDouble("damage-modifier");
        this.damageCauses = new HashSet<>();
        if (section.isList("causes")) {
            for (String str : section.getStringList("causes")) {
                damageCauses.add(EntityDamageEvent.DamageCause.valueOf(str));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        if (!damageCauses.isEmpty() && !damageCauses.contains(event.getCause()))
            return;

        if (!isAffectedByCharm(player))
            return;

        event.setDamage(event.getDamage() * damageModifier);
    }
}
