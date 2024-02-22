package com.golfing8.kcharm.module.animation;

import com.golfing8.kcommon.config.lang.Message;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Used to send a message to players.
 * <p>
 * Due to the robustness of the {@link Message} class,
 * this covers sounds, titles, and action bars too.
 * </p>
 */
public class CharmAnimationMessage extends CharmAnimation {
    /** Sent when a charm's effect activates. */
    private final Message onStart;
    /** Called when a charm is ticking */
    private final Message onTick;
    /** Sent when a charm's effect deactivates. */
    private final Message onEnd;

    public CharmAnimationMessage(ConfigurationSection section) {
        super(section);

        this.onStart = new Message(section.get("on-start"));
        this.onTick = new Message(section.get("on-tick"));
        this.onEnd = new Message(section.get("on-end"));
    }

    @Override
    public void onActivate(Player holdingCharm, Set<Player> affectedPlayers) {
        if (onStart.isEmpty())
            return;

        affectedPlayers.forEach(onStart::send);
    }

    @Override
    public void onTick(Player holdingCharm, Set<Player> affectedPlayers) {
        if (onTick.isEmpty())
            return;

        affectedPlayers.forEach(onTick::send);
    }

    @Override
    public void onDeactivate(Player holdingCharm, Set<Player> affectedPlayers) {
        if (onEnd.isEmpty())
            return;

        affectedPlayers.forEach(onEnd::send);
    }
}
