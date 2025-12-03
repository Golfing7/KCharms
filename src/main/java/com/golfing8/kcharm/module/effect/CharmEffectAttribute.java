package com.golfing8.kcharm.module.effect;

import com.golfing8.kcharm.KCharms;
import com.golfing8.kcharm.module.CharmModule;
import com.google.common.base.Preconditions;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Lets you modify a player's attributes directly.
 */
public class CharmEffectAttribute extends CharmEffect {
    /** Contains all attribute modifiers for certain attributes */
    private final Map<Attribute, List<AttributeModifier>> attributeModifiers;

    public CharmEffectAttribute(String id, ConfigurationSection section) {
        super(id, section);
        Preconditions.checkArgument(section.isConfigurationSection("modifiers"), "Must contain 'modifiers' in config");

        CharmModule module = CharmModule.get();
        this.attributeModifiers = new HashMap<>();
        ConfigurationSection modSection = section.getConfigurationSection("modifiers");
        for (String type : modSection.getKeys(false)) {
            Attribute attribute;
            try {
                attribute = Registry.ATTRIBUTE.getOrThrow(NamespacedKey.minecraft(type));
            } catch (NoSuchElementException exc) {
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

            NamespacedKey key = new NamespacedKey(KCharms.getInstance(), UUID.randomUUID().toString());
            AttributeModifier modifier = new AttributeModifier(key, number, operation);
            attributeModifiers.computeIfAbsent(attribute, (k) -> new ArrayList<>()).add(modifier);
        }
    }

    @Override
    public void startEffect(Player player) {
        for (var entry : attributeModifiers.entrySet()) {
            AttributeInstance instance = player.getAttribute(entry.getKey());
            if (instance == null)
                continue;

            for (AttributeModifier modifier : entry.getValue()) {
                if (instance.getModifier(modifier.getKey()) != null)
                    continue;

                instance.addTransientModifier(modifier);
            }
        }
    }

    @Override
    public void stopEffect(Player player) {
        for (var entry : attributeModifiers.entrySet()) {
            AttributeInstance instance = player.getAttribute(entry.getKey());
            if (instance == null)
                continue;

            for (AttributeModifier modifier : entry.getValue()) {
                if (instance.getModifier(modifier.getKey()) == null)
                    continue;

                instance.removeModifier(modifier);
            }
        }
    }
}
