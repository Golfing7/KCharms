package com.golfing8.kcharm.module.animation;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.shade.com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;

/**
 * An animation that spawns a block display which rides the player's head.
 */
@Getter
public class CharmAnimationBlockDisplay extends CharmAnimationDisplay {
    private final Material material;

    public CharmAnimationBlockDisplay(ConfigurationSection section) {
        super(section);

        this.material = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofString(section.getString("material")), XMaterial.class).parseMaterial();
    }

    @Override
    protected void adaptDisplay(Display display) {
        super.adaptDisplay(display);

        BlockDisplay blockDisplay = (BlockDisplay) display;
        blockDisplay.setBlock(material.createBlockData());
    }

    @Override
    protected BlockDisplay spawnDisplay(Location location) {
        BlockDisplay display = location.getWorld().spawn(location, BlockDisplay.class);
        adaptDisplay(display);
        return display;
    }
}
