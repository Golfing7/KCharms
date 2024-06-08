package com.golfing8.kcharm.module.condition;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.lang.Message;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * An arbitrary condition that can be applied to a part of a charm such as its active or passive ability.
 */
public abstract class CharmCondition implements Predicate<ConditionContext> {
    /** The defining section of this condition */
    protected final ConfigurationSection section;
    @Getter
    protected @Nullable Message failedMessage;
    public CharmCondition(ConfigurationSection section) {
        this.section = section;

        if (section.contains("failed-message")) {
            this.failedMessage = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "failed-message"), Message.class);
        }
    }
}
