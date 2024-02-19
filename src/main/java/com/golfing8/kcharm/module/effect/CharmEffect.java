package com.golfing8.kcharm.module.effect;

import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcharm.module.struct.Charm;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines an effect that a charm has.
 * <p>
 * One charm can have multiple effects 'compounded' together.
 * </p>
 */
public abstract class CharmEffect implements Listener {
    /** All players who are currently holding this charm */
    private final Set<Player> holdingPlayers = new HashSet<>();

    /** Contains all players who are currently under the effect of this charm. */
    private Set<Player> affectedPlayers = new HashSet<>();
    /** The range of efficacy taken from the player's feet */
    @Getter @Setter
    private double effectiveRange = 0.0D;
    /** The players that this can select, if empty defaults to SELF */
    @Getter
    private Set<CharmEffectSelection> effectSelections = Sets.newHashSet(CharmEffectSelection.SELF);

    public CharmEffect(ConfigurationSection section) {
        CharmModule module = CharmModule.get();
        module.addTask(this::tickAffectedPlayers).runTaskTimer(module.getPlugin(), 0, 20);

        if (!section.contains("ranged"))
            return;

        this.effectiveRange = section.getDouble("ranged.range");
        if (section.isList("ranged.players")) {
            for (String str : section.getStringList("ranged.players")) {
                this.effectSelections.add(CharmEffectSelection.valueOf(str));
            }
        }
    }

    private void tickAffectedPlayers() {
        if (effectiveRange <= 0.0D) {
            if (this.effectSelections.contains(CharmEffectSelection.SELF)) {
                for (Player player : this.affectedPlayers) {
                    tickEffect(player);
                }
            }
            return;
        }

        Set<Player> newAffectedPlayers = new HashSet<>();
        for (Player player : holdingPlayers) {
            if (this.effectSelections.contains(CharmEffectSelection.SELF)) {
                tickEffect(player);
            }
            Set<Player> inRange = getAffectedPlayers(player);
            for (Player inRangePlayer : inRange) {
                if (inRangePlayer == player)
                    continue;

                newAffectedPlayers.add(inRangePlayer);
            }
        }

        for (Player player : newAffectedPlayers) {
            if (!this.affectedPlayers.contains(player)) {
                this.startEffect(player);
            } else {
                this.tickEffect(player);
            }
        }

        for (Player player : this.affectedPlayers) {
            if (!newAffectedPlayers.contains(player)) {
                this.stopEffect(player);
            }
        }

        this.affectedPlayers = newAffectedPlayers;
    }

    /**
     * Gets the affected players from the given charm holder.
     *
     * @param player the player holding the charm.
     * @return the affected players.
     */
    public Set<Player> getAffectedPlayers(Player player) {
        Set<Player> all = new HashSet<>();
        effectSelections.forEach(selection -> all.addAll(selection.playerGetter.apply(player, effectiveRange)));
        return all;
    }

    public final void markPlayerHeld(Player player) {
        this.holdingPlayers.add(player);
        this.onStartHolding(player);
        this.startEffect(player);
        this.tickAffectedPlayers();
    }

    public final void stopPlayerHold(Player player) {
        // The order is important as we want isHoldingCharm to be true for when a player stops holding the charm.
        this.onStopHolding(player);
        this.stopEffect(player);
        this.holdingPlayers.remove(player);
        this.tickAffectedPlayers();
    }

    /**
     * Called when a player has begun holding this charm's effect.
     *
     * @param player the player.
     */
    protected void onStartHolding(Player player) {}

    /**
     * Called when a player has stopped holding a charm that has this effect.
     * <p>
     * At this point the charm is still in the player's hand, but about to be removed.
     * </p>
     *
     * @param player the player.
     */
    protected void onStopHolding(Player player) {}

    /**
     * Called when a player should start being affected by this charm.
     *
     * @param player the player that entered the range.
     */
    public void startEffect(Player player) {}

    /**
     * Called when a player is standing in the range of effectivity.
     *
     * @param player the player.
     */
    public void tickEffect(Player player) {}

    /**
     * Called when a player should stop being affected by this charm.
     *
     * @param player the player that exited the range.
     */
    public void stopEffect(Player player) {}

    /**
     * Checks if the given player is holding a charm with this effect.
     *
     * @param player the player.
     * @return if they're holding this charm.
     */
    public boolean isHoldingCharm(Player player) {
        return this.holdingPlayers.contains(player);
    }

    /**
     * Checks if the given player is affected by this charm.
     *
     * @param player the player.
     * @return if they're affected by this charm.
     */
    public boolean isAffectedByCharm(Player player) {
        return (this.holdingPlayers.contains(player) && this.effectSelections.contains(CharmEffectSelection.SELF)) || this.affectedPlayers.contains(player);
    }
}
