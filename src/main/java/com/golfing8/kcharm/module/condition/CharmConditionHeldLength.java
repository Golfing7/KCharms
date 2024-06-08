package com.golfing8.kcharm.module.condition;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.Range;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 * A condition for how long the player has been holding the charm.
 */
public class CharmConditionHeldLength extends CharmCondition {
    private final Range heldLength;

    public CharmConditionHeldLength(ConfigurationSection section) {
        super(section);

        this.heldLength = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "held-length"), Range.class);
    }

    @Override
    public boolean test(ConditionContext conditionContext) {
        int heldLength = Bukkit.getCurrentTick() - conditionContext.startedHoldingTick();
        return this.heldLength.inRange(heldLength);
    }
}
