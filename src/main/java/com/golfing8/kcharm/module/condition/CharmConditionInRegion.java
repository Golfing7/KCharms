package com.golfing8.kcharm.module.condition;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.region.Region;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Checks if a player is within a specific region.
 */
public class CharmConditionInRegion extends CharmCondition {
    private final Region region;

    public CharmConditionInRegion(ConfigurationSection section) {
        super(section);

        this.region = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "region"), Region.class);
    }

    @Override
    public boolean test(ConditionContext conditionContext) {
        return region.isWithin(conditionContext.holder());
    }
}
