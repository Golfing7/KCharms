package com.golfing8.kcharm.module.animation;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.util.MS;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An animation that spawns a block display which rides the player's head.
 */
@Getter
public class CharmAnimationBlockDisplay extends CharmAnimationDisplay {
    private final Material material;

    private final Map<Player, BlockDisplay> spawnedDisplays = new HashMap<>();

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

    @Override
    public void onActivate(Player holdingCharm, Set<Player> affectedPlayers) {
        for (Player player : affectedPlayers) {
            Location playerLocation = player.getLocation();
            playerLocation.setYaw(0.0F);
            playerLocation.setPitch(0.0F);
            BlockDisplay value = spawnDisplay(playerLocation.add(0, 3, 0));
            player.addPassenger(value);
            spawnedDisplays.put(player, value);
        }
    }

    @Override
    public void onDeactivate(Player holdingCharm, Set<Player> affectedPlayers) {
        for (Player player : affectedPlayers) {
            if (!spawnedDisplays.containsKey(player))
                continue;

            spawnedDisplays.remove(player).remove();
        }
    }

    @Override
    public void stopEffect(Player player) {
        if (!spawnedDisplays.containsKey(player))
            return;

        spawnedDisplays.remove(player).remove();
    }
}
