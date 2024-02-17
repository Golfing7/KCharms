package com.golfing8.kcharm.module;

import com.golfing8.kcharm.module.cmd.CharmCommand;
import com.golfing8.kcharm.module.effect.CharmEffect;
import com.golfing8.kcharm.module.effect.CharmType;
import com.golfing8.kcharm.module.struct.Charm;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.ModuleInfo;
import de.tr7zw.kcommon.nbtapi.NBTItem;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
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
            CharmEffect effect = CharmType.fromConfig(effectSection.getConfigurationSection(charmEffect));
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
        if (!nbtItem.hasTag(CHARM_ITEM_KEY))
            return Collections.emptyList();

        List<String> charmIDs = nbtItem.getStringList(CHARM_ITEM_KEY);
        List<Charm> charms = new ArrayList<>();
        for (String str : charmIDs) {
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
        return new ItemStack[] {
                player.getInventory().getItemInMainHand(),
                player.getInventory().getItemInOffHand()
        };
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemHeld(PlayerSwapHandItemsEvent event) {
        ItemStack mainHandItem = event.getMainHandItem();
        for (Charm charm : getCharms(mainHandItem)) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.onStopHolding(event.getPlayer());
            }
        }

        ItemStack offHandItem = event.getOffHandItem();
        for (Charm charm : getCharms(offHandItem)) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.onStartHolding(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getSlot() != 40 || !(event.getWhoClicked() instanceof Player player))
            return;

        if (event.getClickedInventory().getType() != InventoryType.PLAYER)
            return;

        ItemStack cursor = event.getCursor();
        for (Charm charm : getCharms(cursor)) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.onStopHolding(player);
            }
        }

        ItemStack current = event.getCurrentItem();
        for (Charm charm : getCharms(current)) {
            for (CharmEffect effect : charm.charmEffects()) {
                effect.onStartHolding(player);
            }
        }
    }
}
