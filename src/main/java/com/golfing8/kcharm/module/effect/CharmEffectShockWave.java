package com.golfing8.kcharm.module.effect;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.shade.com.cryptomorin.xseries.XMaterial;
import com.google.gson.reflect.TypeToken;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A charm effect for allowing shockwave (trench) pickaxes.
 */
public class CharmEffectShockWave extends CharmEffect {
    private final int radius;
    private final Set<XMaterial> blockWhitelist;
    private final Set<XMaterial> blockBlacklist;

    public CharmEffectShockWave(String id, ConfigurationSection section) {
        super(id, section);

        this.blockWhitelist = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "block-whitelist"), FieldType.extractFrom(new TypeToken<Set<XMaterial>>() {}));
        this.blockBlacklist = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "block-blacklist"), FieldType.extractFrom(new TypeToken<Set<XMaterial>>() {}));
        this.radius = section.getInt("radius", 1);
    }

    /**
     * Checks if the material is allowed to be used
     *
     * @param material the material
     * @return true if allowed
     */
    private boolean isMaterialAllowed(XMaterial material) {
        if (!this.blockWhitelist.isEmpty() && this.blockWhitelist.contains(material)) {
            return true;
        }
        return this.blockBlacklist.isEmpty() || !this.blockBlacklist.contains(material);
    }

    /** If the event is currently silenced */
    private transient boolean silenceEvent = false;
    private transient @Nullable List<Item> capturedDrops;
    private transient int capturedXp;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent event) {
        if (silenceEvent && capturedDrops != null) {
            capturedDrops.addAll(event.getItems());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        if (silenceEvent) {
            capturedXp += event.getExpToDrop();
            event.setExpToDrop(0);
            return;
        }

        if (!isAffectedByCharm(event.getPlayer()))
            return;

        try {
            silenceEvent = true;
            capturedXp = 0;
            capturedDrops = new ArrayList<>();

            for (int mx = -radius; mx <= radius; mx++) {
                for (int my = -radius; my <= radius; my++) {
                    for (int mz = -radius; mz <= radius; mz++) {
                        if (mx == 0 && my == 0 && mz == 0)
                            continue;

                        Block relative = event.getBlock().getRelative(mx, my, mz);
                        event.getPlayer().breakBlock(relative);
                    }
                }
            }

            Location dropLocation = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
            for (Item item : capturedDrops) {
                item.teleport(dropLocation);
            }
            if (capturedXp > 0) {
                event.getPlayer().getWorld().spawn(dropLocation, ExperienceOrb.class, orb -> orb.setExperience(capturedXp));
            }
        } finally {
            silenceEvent = false;
            capturedXp = 0;
            capturedDrops = null;
        }
    }
}
