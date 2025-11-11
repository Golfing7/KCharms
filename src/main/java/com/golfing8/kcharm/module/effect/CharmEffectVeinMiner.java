package com.golfing8.kcharm.module.effect;

import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.shade.com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Preconditions;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.blocks.Blocks;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * A charm effect for allowing vein miner for block types.
 */
public class CharmEffectVeinMiner extends CharmEffect {
    private final int maxBlocks;
    private final Set<XMaterial> blockWhitelist;

    public CharmEffectVeinMiner(ConfigurationSection section) {
        super(section);

        this.blockWhitelist = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "block-whitelist"), FieldType.extractFrom(new TypeToken<Set<XMaterial>>() {}));
        this.maxBlocks = section.getInt("max-blocks", 20);
    }

    /** If the event is currently silenced */
    private transient boolean silenceEvent = false;
    private transient @Nullable List<Item> capturedDrops;
    private transient int capturedXp;

    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        if (silenceEvent && capturedDrops != null) {
            capturedDrops.addAll(event.getItems());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (silenceEvent) {
            capturedXp += event.getExpToDrop();
            event.setExpToDrop(0);
            return;
        }

        Block origin = event.getBlock();
        XMaterial type = XMaterial.matchXMaterial(origin.getType());
        if (!blockWhitelist.contains(type))
            return;

        if (!isEffectActive(event.getPlayer()))
            return;

        Queue<Block> blocks = collectBlocksInVein(type, origin);
        try {
            silenceEvent = true;
            capturedXp = 0;
            capturedDrops = new ArrayList<>();
            for (Block block : blocks) {
                if (block == origin)
                    continue;

                event.getPlayer().breakBlock(block);
            }

            Location dropLocation = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
            for (Item item : capturedDrops) {
                item.teleport(dropLocation);
            }
            event.getPlayer().getWorld().spawn(dropLocation, ExperienceOrb.class, orb -> orb.setExperience(capturedXp));
        } finally {
            silenceEvent = false;
            capturedXp = 0;
            capturedDrops = null;
        }
    }

    private Queue<Block> collectBlocksInVein(XMaterial matchType, Block origin) {
        Set<Block> blocks = new HashSet<>();
        Queue<Block> blocksToHandle = new ArrayDeque<>();
        blocksToHandle.add(origin);
        while (!blocksToHandle.isEmpty()) {
            for (BlockFace face : BlockFace.values()) {
                if (blocks.size() >= maxBlocks)
                    return blocksToHandle;

                if (face == BlockFace.SELF)
                    continue;

                Block other = origin.getRelative(face);
                if (blocks.contains(other) || XMaterial.matchXMaterial(other.getType()) != matchType)
                    continue;

                blocks.add(other);
                blocksToHandle.add(other);
            }
        }
        return blocksToHandle;
    }
}
