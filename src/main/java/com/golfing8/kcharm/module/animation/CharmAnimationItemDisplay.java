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

    private final Map<Player, ItemDisplay> spawnedDisplays = new HashMap<>();

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

    @Override
    public void onActivate(Player holdingCharm, Set<Player> affectedPlayers) {
        for (Player player : affectedPlayers) {
            Location playerLocation = player.getLocation();
            playerLocation.setYaw(0.0F);
            playerLocation.setPitch(0.0F);
            ItemDisplay value = spawnDisplay(playerLocation.add(0, 3, 0));
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
