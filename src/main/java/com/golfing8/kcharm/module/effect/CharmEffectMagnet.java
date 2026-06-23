package com.golfing8.kcharm.module.effect;

import com.golfing8.kcommon.util.PlayerUtil;
import com.golfing8.kcommon.util.SetExpFix;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.*;

/**
 * A charm effect for allowing magnetized block and mob drops.
 */
public class CharmEffectMagnet extends CharmEffect {
    private final boolean magnetBlockDrops;
    private final boolean magnetBlockXp;
    private final boolean magnetMobDrops;
    private final boolean magnetMobXp;
    private final boolean magnetPlayerDrops;
    private final boolean magnetPlayerXp;

    public CharmEffectMagnet(String id, ConfigurationSection section) {
        super(id, section);

        this.magnetBlockDrops = section.getBoolean("block-drops", true);
        this.magnetBlockXp = section.getBoolean("block-xp", true);
        this.magnetMobDrops = section.getBoolean("mob-drops", true);
        this.magnetMobXp = section.getBoolean("mob-xp", true);
        this.magnetPlayerDrops = section.getBoolean("player-drops", true);
        this.magnetPlayerXp = section.getBoolean("player-xp", true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent event) {
        if (!magnetBlockDrops)
            return;

        if (!isAffectedByCharm(event.getPlayer()))
            return;

        PlayerUtil.givePlayerItemsSafe(event.getPlayer(), event.getItems().stream().filter(Entity::isValid).map(Item::getItemStack).toList());
        event.getItems().clear();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockXp(BlockBreakEvent event) {
        if (!magnetBlockXp)
            return;

        if (!isAffectedByCharm(event.getPlayer()))
            return;

        int currentXp = SetExpFix.getTotalExperience(event.getPlayer());
        SetExpFix.setTotalExperience(event.getPlayer(), currentXp + event.getExpToDrop());
        event.setExpToDrop(0);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMobDropAndXp(EntityDeathEvent event) {
        boolean player = event.getEntity() instanceof Player;
        Player killer = event.getEntity().getKiller();
        if (killer == null)
            return;

        if (!isAffectedByCharm(killer))
            return;

        if (player && magnetPlayerDrops || !player && magnetMobDrops) {
            PlayerUtil.givePlayerItemsSafe(killer, event.getDrops());
            event.getDrops().clear();
        }

        if (player && magnetPlayerXp || !player && magnetMobXp) {
            int currentXp = SetExpFix.getTotalExperience(killer);
            SetExpFix.setTotalExperience(killer, currentXp + event.getDroppedExp());
            event.setDroppedExp(0);
        }
    }
}
