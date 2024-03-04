package com.golfing8.kcharm.module.animation;

import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcharm.module.effect.CharmEffect;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import io.papermc.paper.entity.TeleportFlag;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An abstract parent class for block, item, and text display animations.
 */
@Getter
public abstract class CharmAnimationDisplay extends CharmAnimation {
    private final Vector3f translation;
    private final Vector3f scale;
    private final AxisAngle4f leftRotation, rightRotation;
    private final int interpolationDuration;
    private final int interpolationDelay;
    private final int teleportDuration;
    private final float viewRange;
    private final float shadowRadius;
    private final float shadowStrength;
    private final float displayWidth, displayHeight;
    private final Display.Billboard billboard;

    // Internal config stuff.
    /** If the display's yaw should be updated to always reflect the player's yaw. */
    private boolean matchPlayerYaw;
    /** If the display's pitch should be updated to always reflect the player's yaw. */
    private boolean matchPlayerPitch;

    protected final Map<Player, Display> spawnedDisplays = new HashMap<>();

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

        if (section.contains("left-rotation")) {
            Vector translation = ConfigTypeRegistry.getFromType(ConfigPrimitive.of(section.get("left-rotation.vector")), Vector.class);
            double angle = Math.toRadians(section.getDouble("left-rotation.angle"));
            this.leftRotation = new AxisAngle4f((float) angle, (float) translation.getX(), (float) translation.getY(), (float) translation.getZ());
        } else {
            this.leftRotation = new AxisAngle4f();
        }

        if (section.contains("right-rotation")) {
            Vector translation = ConfigTypeRegistry.getFromType(ConfigPrimitive.of(section.get("right-rotation.vector")), Vector.class);
            double angle = Math.toRadians(section.getDouble("right-rotation.angle"));
            this.rightRotation = new AxisAngle4f((float) angle, (float) translation.getX(), (float) translation.getY(), (float) translation.getZ());
        } else {
            this.rightRotation = new AxisAngle4f();
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

        this.matchPlayerYaw = section.getBoolean("match-player-yaw");
        this.matchPlayerPitch = section.getBoolean("match-player-pitch");

        CharmModule module = CharmModule.get();
        module.addTask(this::internalTick).runTaskTimer(module.getPlugin(), 0, 1);
    }

    private void internalTick() {
        if (!matchPlayerYaw && !matchPlayerPitch)
            return;

        for (var entry : this.spawnedDisplays.entrySet()) {
            Location location = entry.getKey().getLocation();
            if (!matchPlayerYaw)
                location.setYaw(0.0F);
            if (!matchPlayerPitch)
                location.setPitch(0.0F);
            entry.getValue().teleport(location, TeleportFlag.EntityState.RETAIN_VEHICLE);
        }
    }

    /**
     * Adapts the given display to this object's specifications.
     *
     * @param display the display.
     */
    protected void adaptDisplay(Display display) {
        display.setPersistent(false);
        display.setTransformation(new Transformation(translation, this.leftRotation, scale, this.rightRotation));
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

    @Override
    public void onActivate(Player holdingCharm, Set<Player> affectedPlayers) {
        for (Player player : affectedPlayers) {
            Location playerLocation = player.getLocation();
            if (!matchPlayerYaw)
                playerLocation.setYaw(0.0F);
            if (!matchPlayerPitch)
                playerLocation.setPitch(0.0F);
            Display value = spawnDisplay(playerLocation.add(0, 3, 0));
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
