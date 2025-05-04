package com.golfing8.kcharm.module.condition;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Checks if a player has a specific permission
 */
public class CharmConditionPermission extends CharmCondition {
    private final String permission;

    public CharmConditionPermission(ConfigurationSection section) {
        super(section);

        this.permission = section.getString("permission");
    }

    @Override
    public boolean test(ConditionContext conditionContext) {
        return conditionContext.holder().hasPermission(permission);
    }
}
