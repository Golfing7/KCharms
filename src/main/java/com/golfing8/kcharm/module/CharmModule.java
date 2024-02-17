package com.golfing8.kcharm.module;

import com.golfing8.kcharm.module.struct.Charm;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.ModuleInfo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Charms are items that players can hold in their offhand which give them
 * various buffs/resistances. These charms can range from simple potion effects,
 * harvesting buffs, complex pvp buffs, etc.
 */
@ModuleInfo(
        name = "charm"
)
public class CharmModule extends Module {

    /** All loaded charms */
    private Map<String, Charm> charms;
    /** A lookup map for finding charms faster */
    private Map<Material, List<Charm>> typeToCharm;

    @Override
    public void onEnable() {
        this.typeToCharm = new HashMap<>();
    }

    @Override
    public void onDisable() {

    }

    /**
     * Gets the items from a player that can potentially be charms.
     * <p>
     * The purpose of this method is to allow for configurability of which hand (or both) can be holding charms.
     * </p>
     *
     * @return the itemstack.
     */
    public ItemStack[] getPotentialCharmSlots(Player player) {
        return new ItemStack[] {
                player.getInventory().getItemInMainHand(),
                player.getInventory().getItemInOffHand()
        };
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemHeld(PlayerSwapHandItemsEvent event) {

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlot() != 40)
            return;

        ItemStack cursor = event.getCursor();
        if (cursor != null) {

        }

        ItemStack current = event.getCurrentItem();
        if (cursor != null) {

        }
    }
}
