package com.golfing8.kcharm.module.effect.event;

import com.golfing8.kcharm.module.effect.CharmEffect;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

/**
 * A special API event called when a charm effect does something
 */
public abstract class CharmEffectEvent extends PlayerEvent {
    @Getter
    private final CharmEffect charmEffect;

    public CharmEffectEvent(Player player, CharmEffect charmEffect) {
        super(player);
        this.charmEffect = charmEffect;
    }
}
