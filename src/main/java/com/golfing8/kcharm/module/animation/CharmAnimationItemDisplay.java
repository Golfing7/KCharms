package com.golfing8.kcharm.module.animation;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An animation that spawns an item display which rides the player's head.
 */
@Getter
public class CharmAnimationItemDisplay extends CharmAnimationDisplay {
    private final ItemStackBuilder item;
    private final ItemDisplay.ItemDisplayTransform transform;

    public CharmAnimationItemDisplay(ConfigurationSection section) {
        super(section);

        this.item = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofSection(section.getConfigurationSection("item")), ItemStackBuilder.class);
        this.transform = section.contains("item-display-transform") ?
                ItemDisplay.ItemDisplayTransform.valueOf(section.getString("item-display-transform")) :
                ItemDisplay.ItemDisplayTransform.NONE;
    }

    @Override
    protected void adaptDisplay(Display display) {
        super.adaptDisplay(display);

        ItemDisplay itemDisplay = (ItemDisplay) display;
        itemDisplay.setItemStack(item.buildCached());
        itemDisplay.setItemDisplayTransform(transform);
    }

    @Override
    protected ItemDisplay spawnDisplay(Location location) {
        ItemDisplay display = location.getWorld().spawn(location, ItemDisplay.class);
        adaptDisplay(display);
        return display;
    }
}
