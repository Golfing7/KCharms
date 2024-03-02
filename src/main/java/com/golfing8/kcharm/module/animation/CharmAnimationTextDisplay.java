package com.golfing8.kcharm.module.animation;

import com.golfing8.kcommon.util.MS;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An animation that spawns a text display that rides the player's head.
 */
@Getter
public class CharmAnimationTextDisplay extends CharmAnimationDisplay {
    private final String text;
    private final int lineWidth;
    private final Color bgColor;
    private final byte opacity;
    private final boolean shadowed;
    private final boolean seeThrough;
    private final boolean defaultBG;
    private final TextDisplay.TextAlignment alignment;

    private final Map<Player, TextDisplay> spawnedDisplays = new HashMap<>();

    public CharmAnimationTextDisplay(ConfigurationSection section) {
        super(section);

        this.text = section.getString("text");
        this.lineWidth = section.getInt("line-width", 200);
        this.bgColor = section.contains("bg-color") ? Color.fromRGB(Integer.parseInt(section.getString("bg-color"), 16)) : null;
        this.opacity = (byte) section.getInt("opacity", -1);
        this.shadowed = section.getBoolean("shadowed", false);
        this.seeThrough = section.getBoolean("see-through", false);
        this.defaultBG = section.getBoolean("default-bg", false);
        this.alignment = section.contains("alignment") ? TextDisplay.TextAlignment.valueOf(section.getString("alignment")) : TextDisplay.TextAlignment.CENTER;
    }

    @Override
    protected void adaptDisplay(Display display) {
        super.adaptDisplay(display);

        TextDisplay textDisplay = (TextDisplay) display;
        textDisplay.setText(MS.parseSingle(text));
        textDisplay.setLineWidth(lineWidth);
        textDisplay.setBackgroundColor(bgColor);
        textDisplay.setTextOpacity(opacity);
        textDisplay.setShadowed(shadowed);
        textDisplay.setSeeThrough(seeThrough);
        textDisplay.setDefaultBackground(defaultBG);
        textDisplay.setAlignment(alignment);
    }

    @Override
    protected TextDisplay spawnDisplay(Location location) {
        TextDisplay display = location.getWorld().spawn(location, TextDisplay.class);
        adaptDisplay(display);
        return display;
    }

    @Override
    public void onActivate(Player holdingCharm, Set<Player> affectedPlayers) {
        for (Player player : affectedPlayers) {
            Location playerLocation = player.getLocation();
            playerLocation.setYaw(0.0F);
            playerLocation.setPitch(0.0F);
            TextDisplay value = spawnDisplay(playerLocation.add(0, 3, 0));
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
