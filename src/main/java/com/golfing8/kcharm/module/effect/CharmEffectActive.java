package com.golfing8.kcharm.module.effect;

import com.golfing8.kcharm.KCharms;
import com.golfing8.kcommon.struct.map.CooldownMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * A charm effect that is activated through a player's interaction with
 * a charm.
 */
public abstract class CharmEffectActive extends CharmEffect {
    /** Stores cooldowns for this charm */
    protected final CooldownMap cooldownMap = new CooldownMap(KCharms.getInstance());

    public CharmEffectActive(ConfigurationSection section) {
        super(section);
    }

    /**
     * Called when a player right-click interacts.
     *
     * @param player the player.
     */
    public void onInteract(Player player) {}

    /**
     * Called when a player holding a charm interacts AT another entity.
     *
     * @param interacting the player holding the charm.
     * @param clicked the clicked entity.
     */
    public void onPlayerInteract(Player interacting, Entity clicked) {}
}
