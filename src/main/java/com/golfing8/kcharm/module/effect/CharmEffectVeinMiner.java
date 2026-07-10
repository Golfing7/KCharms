package com.golfing8.kcharm.module.effect;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.shade.com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
 * A charm effect for allowing vein miner for block types.
 */
public class CharmEffectVeinMiner extends CharmEffect {
    private final int maxBlocks;
    private final Set<XMaterial> blockWhitelist;
    private final Map<XMaterial, Set<XMaterial>> blockCategoryWhitelist;

    public CharmEffectVeinMiner(String id, ConfigurationSection section) {
        super(id, section);

        this.blockWhitelist = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "block-whitelist"), FieldType.extractFrom(new TypeToken<Set<XMaterial>>() {}));
        if (section.contains("block-category-whitelist")) {
            this.blockCategoryWhitelist = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "block-category-whitelist"), FieldType.extractFrom(
                    new TypeToken<Map<XMaterial, Set<XMaterial>>>() {}
            ));
        } else {
            this.blockCategoryWhitelist = Collections.emptyMap();
        }
        this.maxBlocks = section.getInt("max-blocks", 20);
    }

    /**
     * Gets the vein mine materials
     *
     * @return the vein mine materials
     */
    private Set<XMaterial> getVeinMineMaterials(XMaterial breaking) {
        if (this.blockWhitelist.contains(breaking)) {
            return Collections.singleton(breaking);
        } else if (this.blockCategoryWhitelist.containsKey(breaking)) {
            Set<XMaterial> whitelisted = this.blockCategoryWhitelist.get(breaking);
            return Sets.union(whitelisted, Collections.singleton(breaking));
        } else {
            return Collections.emptySet();
        }
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

        Block origin = event.getBlock();
        XMaterial type = XMaterial.matchXMaterial(origin.getType());
        Set<XMaterial> materials = getVeinMineMaterials(type);
        if (materials.isEmpty())
            return;

        if (!isAffectedByCharm(event.getPlayer()))
            return;

        Set<Block> blocks = collectBlocksInVein(materials, origin);
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
            if (capturedXp > 0) {
                event.getPlayer().getWorld().spawn(dropLocation, ExperienceOrb.class, orb -> orb.setExperience(capturedXp));
            }
        } finally {
            silenceEvent = false;
            capturedXp = 0;
            capturedDrops = null;
        }
    }

    private Set<Block> collectBlocksInVein(Set<XMaterial> matchTypes, Block origin) {
        Set<Block> blocks = new HashSet<>();
        Queue<Block> blocksToHandle = new ArrayDeque<>();
        blocksToHandle.add(origin);
        while (!blocksToHandle.isEmpty()) {
            Block block = blocksToHandle.poll();
            for (int y = -1; y <= 1; y++) {
                for (BlockFace face : BlockFace.values()) {
                    if (blocks.size() >= maxBlocks)
                        return blocks;

                    if (face == BlockFace.UP || face == BlockFace.DOWN || (face == BlockFace.SELF && y == 0))
                        continue;

                    Block other = block.getRelative(face.getModX(), y, face.getModZ());
                    if (blocks.contains(other) || !matchTypes.contains(XMaterial.matchXMaterial(other.getType())))
                        continue;

                    blocks.add(other);
                    blocksToHandle.add(other);
                }
            }
        }
        return blocks;
    }
}
