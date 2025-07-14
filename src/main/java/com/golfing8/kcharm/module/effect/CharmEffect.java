package com.golfing8.kcharm.module.effect;

import com.golfing8.kcharm.KCharms;
import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcharm.module.animation.CharmAnimation;
import com.golfing8.kcharm.module.animation.CharmAnimationType;
import com.golfing8.kcharm.module.condition.CharmCondition;
import com.golfing8.kcharm.module.condition.CharmConditionType;
import com.golfing8.kcharm.module.condition.ConditionContext;
import com.golfing8.kcharm.module.effect.selection.CharmEffectSelection;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.struct.map.CooldownMap;
import com.golfing8.kcommon.util.ProgressBar;
import com.golfing8.kcommon.util.StringUtil;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Defines an effect that a charm has.
 * <p>
 * One charm can have multiple effects 'compounded' together.
 * </p>
 */
@Getter
public abstract class CharmEffect implements Listener {
    private static final NumberFormat DURATION_FORMAT = new DecimalFormat("###.#");

    /** All players who are currently holding this charm mapped to the tick they started holding it. */
    private final Map<Player, Integer> holdingPlayers = new HashMap<>();
    /** Contains all players who are currently under the effect of this charm. */
    private Set<Player> affectedPlayers = new HashSet<>();
    /** The players that this can select, if empty defaults to SELF */
    private final Set<CharmEffectSelection> effectSelections = Sets.newHashSet(CharmEffectSelection.SELF);
    /** An effective selection predicate that combines all predicates of {@link #effectSelections}. */
    private transient BiPredicate<Player, Player> effectiveSelectionPredicate;
    /** All charm conditions */
    private final Set<CharmCondition> charmConditions = new HashSet<>();
    /** The animations that will play with this effect */
    private final List<CharmAnimation> charmAnimations = new ArrayList<>();
    /** Stores cooldowns for this charm */
    protected final CooldownMap<UUID> cooldownMap = new CooldownMap<>(KCharms.getInstance());
    /** The length in ticks the cooldown should last */
    private int cooldownLengthTicks = -1;
    /** How long the effect *can* work */
    private int effectDurationTicks = -1;
    /** The range of efficacy taken from the player's feet */
    @Setter
    private double effectiveRange = 0.0D;
    /** If the affected players are requried to be vulnerable. */
    private boolean requireVulnerable;
    /** Sent to the user of a charm while they're holding. */
    private Message holdingMsg;
    /** Sent to the user of a charm when it's activated */
    private Message useMsg;
    /** Sent to holders when they're on cooldown, useful for action bars */
    private Message cooldownMsg;
    /** Sent to holders when they're off cooldown */
    private Message offCooldownMsg;

    public CharmEffect(ConfigurationSection section) {
        CharmModule module = CharmModule.get();
        module.addTask(this::tickAffectedPlayers).runTaskTimer(module.getPlugin(), 0, 20);
        module.addTask(this::tickCooldown).runTaskTimer(module.getPlugin(), 0, 2);

        if (section.isConfigurationSection("ranged")) {
            this.effectiveRange = section.getDouble("ranged.range");
            if (section.isList("ranged.players")) {
                this.effectSelections.clear();
                for (String str : section.getStringList("ranged.players")) {
                    this.effectSelections.add(CharmEffectSelection.valueOf(str));
                }
            }
        }

        // Compose the predicate
        this.effectiveSelectionPredicate = (p1, p2) -> {
            for (var type : effectSelections) {
                // This code is cursed.
                if (switch (type) {
                    case SELF -> module.getCharmEffectSelectionManager().getSelector().isSelf(p1, p2);
                    case TEAM -> module.getCharmEffectSelectionManager().getSelector().isAlly(p1, p2);
                    case ENEMY -> module.getCharmEffectSelectionManager().getSelector().isEnemy(p1, p2);
                } ) {
                    return true;
                }
            }
            return false;
        };

        this.holdingMsg = new Message(section.get("holding-message"));
        this.requireVulnerable = section.getBoolean("require-vulnerable", false);

        if (section.isConfigurationSection("active")) {
            this.useMsg = new Message(section.get("active.use-message"));
            this.cooldownMsg = new Message(section.get("active.cooldown-message"));
            this.offCooldownMsg = new Message(section.get("active.off-cooldown-message"));
            this.effectDurationTicks = section.getInt("active.duration", 1);
            this.cooldownLengthTicks = section.getInt("active.cooldown-length");
        }

        if (section.isConfigurationSection("animations")) {
            var animationSection = section.getConfigurationSection("animations");
            for (String subKey : animationSection.getKeys(false)) {
                // The user is trying to delegate here.
                if (animationSection.isString(subKey)) {
                    this.charmAnimations.add(CharmAnimationType.fromConfig(animationSection.getRoot().getConfigurationSection(animationSection.getString(subKey))));
                } else {
                    this.charmAnimations.add(CharmAnimationType.fromConfig(animationSection.getConfigurationSection(subKey)));
                }
            }
        }

        if (section.isConfigurationSection("conditions")) {
            var conditionSection = section.getConfigurationSection("conditions");
            for (String subKey : conditionSection.getKeys(false)) {
                // The user is trying to delegate here.
                if (conditionSection.isString(subKey)) {
                    this.charmConditions.add(CharmConditionType.fromConfig(conditionSection.getRoot().getConfigurationSection(conditionSection.getString(subKey))));
                } else {
                    this.charmConditions.add(CharmConditionType.fromConfig(conditionSection.getConfigurationSection(subKey)));
                }
            }
        }
    }

    /**
     * Called when this charm needs to remove effects from all affected players.
     */
    public void shutdown() {
        for (Player player : this.affectedPlayers) {
            stopEffect(player);
            for (CharmAnimation animation : this.charmAnimations) {
                animation.stopEffect(player);
            }
        }
    }

    /**
     * Ticks all active cooldowns on this effect.
     */
    private void tickCooldown() {
        if (effectDurationTicks <= 0)
            return;

        for (Player player : holdingPlayers.keySet()) {
            if (!cooldownMap.isOnCooldown(player.getUniqueId())) {
                offCooldownMsg.send(player);
                if (!this.charmAnimations.isEmpty()) {
                    var nearbyPlayers = getAffectedPlayers(player);
                    for (CharmAnimation animation : this.charmAnimations) {
                        animation.onDeactivate(player, nearbyPlayers);
                    }
                }
                continue;
            }

            long cooldownLengthMS = cooldownLengthTicks;
            long currentCooldown = cooldownMap.getCooldownRemaining(player.getUniqueId()) / 50L;
            if (this.cooldownMsg != null && !isEffectActive(player)) {
                cooldownMsg.send(player,
                        "PROGRESS_BAR", ProgressBar.getProgressBar(cooldownLengthMS - currentCooldown, cooldownLengthMS, ProgressBar.BOX_UNICODE, 10),
                        "TIME_LEFT", StringUtil.timeFormatted((int) (currentCooldown / 20), true));
            }
        }
    }

    /**
     * Ticks and updated affected players.
     */
    private void tickAffectedPlayers() {
        Set<Player> newAffectedPlayers = new HashSet<>();
        for (var entry : holdingPlayers.entrySet()) {
            // Send the message to the player.
            holdingMsg.send(entry.getKey());
            if (!isEffectActive(entry.getKey())) {
                continue;
            }

            Set<Player> inRange = getAffectedPlayers(entry.getKey());
            ConditionContext context = new ConditionContext(entry.getKey(), inRange, entry.getValue());
            if (!testCharmConditions(context, true))
                continue;

            for (CharmAnimation animation : this.charmAnimations) {
                animation.onTick(entry.getKey(), inRange);
            }
            newAffectedPlayers.addAll(inRange);
        }


        // Loop through all the new players and try to either start affecting them, or stop affecting them.
        for (Player player : newAffectedPlayers) {
            if (!this.affectedPlayers.contains(player)) {
                this.startEffect(player);
            } else {
                this.tickEffect(player);
            }
        }

        // Loop through all the old players and try to stop affecting them if the new set doesn't contain them.
        for (Player player : this.affectedPlayers) {
            if (!newAffectedPlayers.contains(player)) {
                this.stopEffect(player);
                for (CharmAnimation animation : this.charmAnimations) {
                    animation.stopEffect(player);
                }
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
        if (effectiveSelectionPredicate.test(player, player)) {
            all.add(player);
        }
        for (Player other : player.getLocation().getNearbyEntitiesByType(Player.class, effectiveRange)) {
            if (effectiveSelectionPredicate.test(player, other))
                all.add(other);
        }
        return all.stream().filter(p -> !this.requireVulnerable || NMS.getTheNMS().getWGHook().canBeDamaged(p)).collect(Collectors.toSet());
    }

    public final void markPlayerHeld(Player player) {
        this.holdingPlayers.put(player, Bukkit.getCurrentTick());
        this.onStartHolding(player);
        this.tickAffectedPlayers();
    }

    public final void stopPlayerHold(Player player) {
        // The order is important as we want isHoldingCharm to be true for when a player stops holding the charm.
        this.onStopHolding(player);
        this.holdingPlayers.remove(player);
        this.tickAffectedPlayers();
    }

    /**
     * Meant to handle the event when a player quits the server.
     *
     * @param player the player.
     */
    public final void playerQuit(Player player) {
        if (this.holdingPlayers.remove(player) != null) {
            this.stopPlayerHold(player);
        }

        if (this.affectedPlayers.remove(player)) {
            this.stopEffect(player);
            for (CharmAnimation animation : this.charmAnimations) {
                animation.stopEffect(player);
            }
        }
    }

    /**
     * Called when a player tries to activate an ability on this charm.
     *
     * @param activator the activator of the ability.
     * @return true if the ability activated.
     */
    public boolean tryStartAbility(Player activator) {
        if (this.cooldownMap.isOnCooldown(activator.getUniqueId()) || this.cooldownLengthTicks <= 0)
            return false;

        this.cooldownMap.setCooldown(activator.getUniqueId(), this.cooldownLengthTicks * 50L);
        if (!this.charmAnimations.isEmpty()) {
            var nearbyPlayers = getAffectedPlayers(activator);
            for (CharmAnimation animation : this.charmAnimations) {
                animation.onActivate(activator, nearbyPlayers);
            }
        }
        this.tickAffectedPlayers();
        this.useMsg.send(activator);
        return true;
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

    /**
     * Checks if this effect should be active for the given holder.
     * <p>
     * In the event this is a passive charm effect, this is always {@code true}.
     * If active, this will return {@code true} if the last time the player activated the ability
     * is no later than {@link #effectDurationTicks}.
     * </p>
     *
     * @param holder the player holding the charm.
     * @return if the effect is active.
     */
    public boolean isEffectActive(Player holder) {
        if (this.effectDurationTicks <= 0)
            return true;

        // Players must be on cooldown for it to be active.
        if (!this.cooldownMap.isOnCooldown(holder.getUniqueId()))
            return false;

        long currentCooldown = cooldownMap.getCooldownRemaining(holder.getUniqueId()) / 50L;
        long sinceActivation = cooldownLengthTicks - currentCooldown;
        return sinceActivation <= effectDurationTicks;
    }

    /**
     * Tests all charm conditions.
     *
     * @param context the context.
     * @param verbose if true, sends a fail message.
     * @return if all charm conditions passed.
     */
    public boolean testCharmConditions(ConditionContext context, boolean verbose) {
        for (CharmCondition charmCondition : charmConditions) {
            if (!charmCondition.test(context)) {
                if (verbose && charmCondition.getFailedMessage() != null) {
                    charmCondition.getFailedMessage().send(context.holder());
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given player is holding a charm with this effect.
     *
     * @param player the player.
     * @return if they're holding this charm.
     */
    public boolean isHoldingCharm(Player player) {
        return this.holdingPlayers.containsKey(player);
    }

    /**
     * Checks if the given player is affected by this charm.
     *
     * @param player the player.
     * @return if they're affected by this charm.
     */
    public boolean isAffectedByCharm(Player player) {
        return this.affectedPlayers.contains(player);
    }
}
