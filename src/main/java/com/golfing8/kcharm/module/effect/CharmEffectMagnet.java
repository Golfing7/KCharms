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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent event) {
        if (!magnetBlockDrops)
            return;

        if (!isAffectedByCharm(event.getPlayer()))
            return;

        PlayerUtil.givePlayerItemsSafe(event.getPlayer(), event.getItems().stream().map(Item::getItemStack).toList());
        event.getItems().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockXp(BlockBreakEvent event) {
        if (!magnetBlockXp)
            return;

        if (!isAffectedByCharm(event.getPlayer()))
            return;

        if (event.getExpToDrop() > 0) {
            int currentXp = SetExpFix.getTotalExperience(event.getPlayer());
            SetExpFix.setTotalExperience(event.getPlayer(), currentXp + event.getExpToDrop());
            event.setExpToDrop(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobDropAndXp(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player)
            return;

        Player killer = event.getEntity().getKiller();
        if (killer == null)
            return;

        if (!isAffectedByCharm(killer))
            return;

        if (magnetMobDrops) {
            PlayerUtil.givePlayerItemsSafe(killer, event.getDrops());
            event.getDrops().clear();
        }

        if (magnetMobXp) {
            if (event.getDroppedExp() > 0) {
                int currentXp = SetExpFix.getTotalExperience(killer);
                SetExpFix.setTotalExperience(killer, currentXp + event.getDroppedExp());
                event.setDroppedExp(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null)
            return;

        if (!isAffectedByCharm(killer))
            return;

        if (magnetPlayerDrops && !event.getKeepInventory()) {
            List<ItemStack> list = new ArrayList<>(event.getDrops().stream()
                    .filter(item -> !event.getItemsToKeep().contains(item)).toList());
            PlayerUtil.givePlayerItemsSafe(killer, list);
            event.getDrops().removeAll(list);
        }

        if (magnetPlayerXp && event.getDroppedExp() > 0 && !event.getKeepLevel()) {
            int currentXp = SetExpFix.getTotalExperience(killer);
            SetExpFix.setTotalExperience(killer, currentXp + event.getDroppedExp());
            event.setDroppedExp(0);
        }
    }
}
