package com.golfing8.kcharm.module.effect.event;

import com.golfing8.kcharm.module.effect.CharmEffect;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Called when a magnet charm effect gives drops
 */
public class MagnetDropsEvent extends CharmEffectEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    private final List<ItemStack> itemStacks;
    @Getter @Setter
    private int xp;
    @Getter @Setter
    private boolean cancelled;

    public MagnetDropsEvent(Player player, CharmEffect charmEffect, List<ItemStack> items, int xp) {
        super(player, charmEffect);

        this.itemStacks = items;
        this.xp = xp;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
