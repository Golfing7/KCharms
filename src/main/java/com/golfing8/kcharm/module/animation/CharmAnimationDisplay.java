package com.golfing8.kcharm.module.animation;

import com.golfing8.kcharm.module.effect.CharmEffect;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Set;

/**
 * An abstract parent class for block, item, and text display animations.
 */
@Getter
public abstract class CharmAnimationDisplay extends CharmAnimation {
    private final Vector3f translation;
    private final Vector3f scale;
    private final int interpolationDuration;
    private final int interpolationDelay;
    private final int teleportDuration;
    private final float viewRange;
    private final float shadowRadius;
    private final float shadowStrength;
    private final float displayWidth, displayHeight;
    private final Display.Billboard billboard;

    public CharmAnimationDisplay(ConfigurationSection section) {
        super(section);

        if (section.contains("scale")) {
            Vector scale = ConfigTypeRegistry.getFromType(ConfigPrimitive.of(section.get("scale")), Vector.class);
            this.scale = new Vector3f((float) scale.getX(), (float) scale.getY(), (float) scale.getZ());
        } else {
            this.scale = new Vector3f(1.0F, 1.0F, 1.0F);
        }
        if (section.contains("translation")) {
            Vector translation = ConfigTypeRegistry.getFromType(ConfigPrimitive.of(section.get("translation")), Vector.class);
            this.translation = new Vector3f((float) translation.getX(), (float) translation.getY(), (float) translation.getZ());
        } else {
            this.translation = new Vector3f(0.0F, 0.0F, 0.0F);
        }

        this.interpolationDuration = section.getInt("interpolation-duration", -1);
        this.interpolationDelay = section.getInt("interpolation-delay", -1);
        this.teleportDuration = section.getInt("teleport-duration", -1);
        this.viewRange = (float) section.getDouble("view-range", -1D);
        this.shadowRadius = (float) section.getDouble("shadow-radius", -1D);
        this.shadowStrength = (float) section.getDouble("shadow-strength", -1D);
        this.displayWidth = (float) section.getDouble("display-width", -1D);
        this.displayHeight = (float) section.getDouble("display-height", -1D);
        this.billboard = section.contains("billboard") ? Display.Billboard.valueOf(section.getString("billboard")) : Display.Billboard.FIXED;
    }

    /**
     * Adapts the given display to this object's specifications.
     *
     * @param display the display.
     */
    protected void adaptDisplay(Display display) {
        display.setPersistent(false);
        display.setTransformation(new Transformation(translation, new Quaternionf(), scale, new Quaternionf()));
        if (teleportDuration >= 0)
            display.setTeleportDuration(teleportDuration);
        if (interpolationDuration >= 0)
            display.setInterpolationDuration(interpolationDuration);
        if (interpolationDelay >= 0)
            display.setInterpolationDelay(interpolationDelay);
        if (viewRange >= 0)
            display.setViewRange(viewRange);
        if (shadowRadius >= 0)
            display.setShadowRadius(shadowRadius);
        if (shadowStrength >= 0)
            display.setShadowStrength(shadowStrength);
        if (displayWidth >= 0)
            display.setDisplayWidth(displayWidth);
        if (displayHeight >= 0)
            display.setDisplayHeight(displayHeight);
        display.setBillboard(billboard);
    }

    /**
     * Spawns a display at the given location.
     *
     * @param location the location.
     * @return the display.
     */
    protected abstract Display spawnDisplay(Location location);
}
