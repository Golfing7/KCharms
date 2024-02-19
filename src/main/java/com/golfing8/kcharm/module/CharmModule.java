package com.golfing8.kcharm.module;

import com.golfing8.kcharm.module.cmd.CharmCommand;
import com.golfing8.kcharm.module.effect.CharmEffect;
import com.golfing8.kcharm.module.effect.CharmEffectType;
import com.golfing8.kcharm.module.struct.Charm;
import com.golfing8.kcommon.config.generator.Conf;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.ModuleInfo;
import com.golfing8.kcommon.module.ModuleTask;
import de.tr7zw.kcommon.nbtapi.NBTCompound;
import de.tr7zw.kcommon.nbtapi.NBTItem;
import de.tr7zw.kcommon.nbtapi.NBTType;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Charms are items that players can hold in their offhand which give them
 * various buffs/resistances. These charms can range from simple potion effects,
 * harvesting buffs, complex pvp buffs, etc.
 */
@ModuleInfo(
        name = "charms"
)
public class CharmModule extends Module {
    public static final String CHARM_ITEM_KEY = "kcharm-type";

    @Getter
    @Conf("Allow main hand charms")
    private boolean allowMainHandCharms = false;
    /** All loaded charms */
    @Getter
    private Map<String, Charm> charms;
    /** A lookup map for finding charms faster */
    @Getter
    private Map<Material, List<Charm>> typeToCharm;

    /** Contains all the loaded charm effects by ID */
    @Getter
    private Map<String, CharmEffect> charmEffects;

    @Override
    public void onEnable() {
        this.typeToCharm = new HashMap<>();
        this.charms = new HashMap<>();
        this.charmEffects = new HashMap<>();
        ConfigurationSection effectSection = getMainConfig().getConfigurationSection("charm-effects");
        for (String charmEffect : effectSection.getKeys(false)) {
            CharmEffect effect = CharmEffectType.fromConfig(effectSection.getConfigurationSection(charmEffect));
            addSubListener(effect);
            this.charmEffects.put(charmEffect, effect);
        }

        ConfigurationSection charmSection = getMainConfig().getConfigurationSection("charms");
        for (String charmID : charmSection.getKeys(false)) {
            Charm charm = Charm.fromConfig(charmSection.getConfigurationSection(charmID));
            this.charms.put(charmID, charm);
            this.typeToCharm.computeIfAbsent(charm.charmItemFormat().getItemType().parseMaterial(), (k) -> new ArrayList<>()).add(charm);
        }

        this.addCommand(new CharmCommand());
    }

    @Override
    public void onDisable() {

    }

    /**
     * Gets the charms the player is currently holding.
     *
     * @param player the player.
     * @return the charms that they're holding.
     */
    public List<Charm> getHeldCharms(Player player) {
        List<Charm> charms = new ArrayList<>();
        for (ItemStack stack : getCharmItems(player)) {
            charms.addAll(getCharms(stack));
        }
        return charms;
    }

    /**
     * Gets the charms that the item contains.
     *
     * @param stack the stack.
     * @return the charms.
     */
    public List<Charm> getCharms(ItemStack stack) {
        if (stack == null)
            return Collections.emptyList();

        if (!this.typeToCharm.containsKey(stack.getType()))
            return Collections.emptyList();

        NBTItem nbtItem = new NBTItem(stack);
        if (!nbtItem.hasTag(CHARM_ITEM_KEY, NBTType.NBTTagCompound))
            return Collections.emptyList();

        NBTCompound compound = nbtItem.getCompound(CHARM_ITEM_KEY);
        List<Charm> charms = new ArrayList<>();
        for (String str : compound.getKeys()) {
            if (!this.charms.containsKey(str)) // Filter dead IDs
                continue;

            charms.add(this.charms.get(str));
        }

        return charms;
    }

    /**
     * Gets the items from a player that can potentially be charms.
     * <p>
     * The purpose of this method is to allow for configurability of which hand (or both) can be holding charms.
     * </p>
     *
     * @return the itemstack.
     */
    public ItemStack[] getCharmItems(Player player) {
        if (allowMainHandCharms) {
            return new ItemStack[] {
                    player.getInventory().getItemInMainHand(),
                    player.getInventory().getItemInOffHand()
            };
        } else {
            return new ItemStack[] {
                    player.getInventory().getItemInOffHand()
            };
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemHeld(PlayerSwapHandItemsEvent event) {
        if (allowMainHandCharms)
            return;

        ItemStack mainHandItem = event.getMainHandItem();
        for (Charm charm : getCharms(mainHandItem)) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.stopPlayerHold(event.getPlayer());
            }
        }

        ItemStack offHandItem = event.getOffHandItem();
        for (Charm charm : getCharms(offHandItem)) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.markPlayerHeld(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHeld(PlayerItemHeldEvent event) {
        if (!allowMainHandCharms)
            return;

        ItemStack oldItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
        for (Charm charm : getCharms(oldItem)) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.stopPlayerHold(event.getPlayer());
            }
        }

        ItemStack newItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
        for (Charm charm : getCharms(newItem)) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.markPlayerHeld(event.getPlayer());
            }
        }
    }

    // Off-hand click listener
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onOffhandClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getSlot() != 40 || !(event.getWhoClicked() instanceof Player player))
            return;

        if (event.getClickedInventory().getType() != InventoryType.PLAYER)
            return;

        ItemStack current = event.getCurrentItem();
        for (Charm charm : getCharms(current)) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.stopPlayerHold(player);
            }
        }

        ItemStack cursor = event.getCursor();
        for (Charm charm : getCharms(cursor)) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.markPlayerHeld(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMainHandClick(InventoryClickEvent event) {
        if (!allowMainHandCharms || !(event.getWhoClicked() instanceof Player player))
            return;

        if (event.getSlot() == event.getWhoClicked().getInventory().getHeldItemSlot()) {
            List<Charm> currentCharms = getCharms(event.getCurrentItem());
            for (Charm charm : currentCharms) {
                for (CharmEffect effect : charm.charmEffects()) {
                    effect.stopPlayerHold(player);
                }
            }

            List<Charm> cursorCharms = getCharms(event.getCursor());
            for (Charm charm : cursorCharms) {
                for (CharmEffect effect : charm.charmEffects()) {
                    effect.markPlayerHeld(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMainHandDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) ||
                !event.getInventorySlots().contains(event.getWhoClicked().getInventory().getHeldItemSlot()))
            return;

        List<Charm> cursorCharms = getCharms(event.getOldCursor());
        for (Charm charm : cursorCharms) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.markPlayerHeld(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        ItemStack clickedWithItem = event.getItem();
        for (Charm charm : getCharms(clickedWithItem)) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.onInteract(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        for (Charm charm : getHeldCharms(event.getPlayer())) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.onPlayerInteract(event.getPlayer(), event.getRightClicked());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (Charm charm : getHeldCharms(event.getPlayer())) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.stopPlayerHold(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (Charm charm : getHeldCharms(event.getPlayer())) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.markPlayerHeld(event.getPlayer());
            }
        }
    }

    @Override
    public synchronized ModuleTask addTask(Runnable runnable) {
        return super.addTask(runnable);
    }
}
