package com.golfing8.kcharm.module.effect;

import com.golfing8.kcharm.module.CharmModule;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Lets you modify a player's attributes directly.
 */
@AllArgsConstructor
public class CharmEffectAttribute extends CharmEffect {
    /** Contains all attribute modifiers for certain attributes */
    private final Map<Attribute, List<AttributeModifier>> attributeModifiers;

    @Override
    public void onStartHolding(Player player) {
        for (var entry : attributeModifiers.entrySet()) {
            AttributeInstance instance = player.getAttribute(entry.getKey());
            if (instance == null)
                continue;

            for (AttributeModifier modifier : entry.getValue()) {
                if (instance.getModifier(modifier.getUniqueId()) != null)
                    continue;

                instance.addTransientModifier(modifier);
            }
        }
    }

    @Override
    public void onStopHolding(Player player) {
        for (var entry : attributeModifiers.entrySet()) {
            AttributeInstance instance = player.getAttribute(entry.getKey());
            if (instance == null)
                continue;

            for (AttributeModifier modifier : entry.getValue()) {
                if (instance.getModifier(modifier.getUniqueId()) == null)
                    continue;

                instance.removeModifier(modifier);
            }
        }
    }

    /**
     * Loads an instance of this from the config section.
     *
     * @param section the config section.
     * @return the charm effect instance
     */
    public static CharmEffectAttribute fromConfig(ConfigurationSection section) {
        Preconditions.checkArgument(section.isConfigurationSection("modifiers"), "Must contain 'modifiers' in config");

        CharmModule module = CharmModule.get();
        Map<Attribute, List<AttributeModifier>> attributeModifiers = new HashMap<>();
        ConfigurationSection modSection = section.getConfigurationSection("modifiers");
        for (String type : modSection.getKeys(false)) {
            Attribute attribute;
            try {
                attribute = Attribute.valueOf(type);
            } catch (IllegalArgumentException exc) {
                module.getPlugin().getLogger().warning("Attribute of type '%s' doesn't exist.".formatted(type));
                continue;
            }

            ConfigurationSection attributeSection = modSection.getConfigurationSection(type);
            double number = attributeSection.getDouble("number");
            AttributeModifier.Operation operation;
            try {
                operation = AttributeModifier.Operation.valueOf(attributeSection.getString("operation"));
            } catch (IllegalArgumentException exc) {
                module.getPlugin().getLogger().warning("Attribute operation '%s' does not exist.".formatted(attributeSection.getString("operation")));
                continue;
            }

            UUID uuid = UUID.randomUUID();
            AttributeModifier modifier = new AttributeModifier(uuid, uuid.toString(), number, operation);
            attributeModifiers.computeIfAbsent(attribute, (k) -> new ArrayList<>()).add(modifier);
        }
        return new CharmEffectAttribute(attributeModifiers);
    }
}
