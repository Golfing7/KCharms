package com.golfing8.kcharm.module.condition;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.Range;
import org.bukkit.configuration.ConfigurationSection;

/**
 * A condition for checking that there are X many affected players.
 */
public class CharmConditionAffectedPlayers extends CharmCondition {
    private final Range acceptableRange;

    public CharmConditionAffectedPlayers(ConfigurationSection section) {
        super(section);

        this.acceptableRange = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "range"), Range.class);
    }

    @Override
    public boolean test(ConditionContext conditionContext) {
        return acceptableRange.inRange(conditionContext.applicablePlayers().size());
    }
}
