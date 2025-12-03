package com.golfing8.kcharm.module;

import com.golfing8.kcharm.module.cmd.CharmCommand;
import com.golfing8.kcharm.module.effect.CharmEffect;
import com.golfing8.kcharm.module.effect.CharmEffectType;
import com.golfing8.kcharm.module.effect.selection.CharmEffectSelectionManager;
import com.golfing8.kcharm.module.struct.Charm;
import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.config.generator.Conf;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.ModuleInfo;
import com.golfing8.kcommon.struct.map.CooldownMap;
import com.golfing8.kcommon.util.ItemUtil;
import de.tr7zw.kcommon.nbtapi.NBT;
import de.tr7zw.kcommon.nbtapi.NBTList;
import de.tr7zw.kcommon.nbtapi.NBTType;
import de.tr7zw.kcommon.nbtapi.iface.ReadableNBT;
import de.tr7zw.kcommon.nbtapi.iface.ReadableNBTList;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

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
    public static final String CHARM_EFFECT_LIST = "kcharm-effects";

    @Getter
    @Conf("Allow main hand charms")
    private boolean allowMainHandCharms = false;
    /** All loaded charms */
    @Getter
    private Map<String, Charm> charms;
    /** Contains all the loaded charm effects by ID */
    @Getter
    private Map<String, CharmEffect> charmEffects;

    @Getter
    private CharmEffectSelectionManager charmEffectSelectionManager;

    /** Used to prevent players from accidentally activating abilities */
    private CooldownMap<UUID> cantActivateAbilities;

    @Override
    public void onEnable() {
        this.charms = new HashMap<>();
        this.charmEffects = new HashMap<>();
        this.charmEffectSelectionManager = new CharmEffectSelectionManager();
        this.cantActivateAbilities = new CooldownMap<>();
        for (Configuration configuration : loadConfigGroup("effects")) {
            CharmEffect effect = CharmEffectType.fromConfig(configuration.getFileNameNoExtension(), configuration);
            addSubListener(effect);
            this.charmEffects.put(configuration.getFileNameNoExtension(), effect);
        }

        ConfigurationSection charmSection = getMainConfig().getConfigurationSection("charms");
        for (String charmID : charmSection.getKeys(false)) {
            Charm charm = Charm.fromConfig(charmSection.getConfigurationSection(charmID));
            this.charms.put(charmID, charm);
        }

        this.addCommand(new CharmCommand());

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (CharmEffect effect : getHeldCharmEffects(player)) {
                effect.markPlayerHeld(player);
            }
        }

        // Register a task to garbage collect abilities that shouldn't be applied.
        addTask(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Set<CharmEffect> actualEffects = getHeldCharmEffects(player);

                for (CharmEffect effect : this.charmEffects.values()) {
                    if (effect.isHoldingCharm(player) && !actualEffects.contains(effect)) {
                        effect.stopPlayerHold(player);
                    }
                }
            }
        }).startTimer(0, 20);
    }

    @Override
    public void onDisable() {
        for (CharmEffect effect : this.charmEffects.values()) {
            effect.shutdown();
        }
    }

    /**
     * Gets the charms the player is currently holding.
     *
     * @param player the player.
     * @return the charms that they're holding.
     */
    public Set<CharmEffect> getHeldCharmEffects(Player player) {
        Set<CharmEffect> charms = new LinkedHashSet<>();
        for (ItemStack stack : getCharmItems(player)) {
            charms.addAll(getCharmEffects(stack));
        }
        return charms;
    }

    /**
     * Gets the charms that the item contains.
     *
     * @param stack the stack.
     * @return the charms.
     */
    public List<CharmEffect> getCharmEffects(ItemStack stack) {
        if (ItemUtil.isAirOrNull(stack) || !stack.hasItemMeta())
            return Collections.emptyList();

        ReadableNBT nbt = NBT.readNbt(stack);
        List<CharmEffect> charmEffects = new ArrayList<>();
        if (nbt.hasTag(CHARM_ITEM_KEY, NBTType.NBTTagCompound)) {
            ReadableNBT compound = nbt.getCompound(CHARM_ITEM_KEY);
            for (String str : compound.getKeys()) {
                if (!this.charms.containsKey(str)) // Filter dead IDs
                    continue;

                charmEffects.addAll(this.charms.get(str).charmEffects());
            }
        } else if (nbt.hasTag(CHARM_EFFECT_LIST, NBTType.NBTTagList)) {
            ReadableNBTList<String> effectList = nbt.getStringList(CHARM_EFFECT_LIST);
            for (String str : effectList) {
                if (this.charmEffects.containsKey(str)) {
                    charmEffects.add(this.charmEffects.get(str));
                }
            }
        }

        return charmEffects;
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

        updateHeldCharms(event.getPlayer(), event.getMainHandItem(), event.getOffHandItem());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHeld(PlayerItemHeldEvent event) {
        if (!allowMainHandCharms)
            return;

        updateHeldCharms(event.getPlayer(), event.getPlayer().getInventory().getItem(event.getPreviousSlot()), event.getPlayer().getInventory().getItem(event.getNewSlot()));
    }

    private static ItemStack getCursoredItem(InventoryClickEvent event) {
        return event.getClick() == ClickType.NUMBER_KEY ? event.getWhoClicked().getInventory().getItem(event.getHotbarButton()) : event.getCursor();
    }

    // Off-hand click listener
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onOffhandClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || !(event.getWhoClicked() instanceof Player player))
            return;

        if (event.getClickedInventory().getType() != InventoryType.PLAYER)
            return;

        // Update the held charms of the player.
        addTask(() -> {
            Set<CharmEffect> allHeldCharms = getHeldCharmEffects(player);
            updateHeldCharms(player, allHeldCharms);
        }).start();
    }

    // Off-hand click listener
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onOffhandClick(InventoryDragEvent event) {
        if (!event.getInventorySlots().contains(40) || !(event.getWhoClicked() instanceof Player player))
            return;

        if (event.getInventory().getType() != InventoryType.PLAYER)
            return;

        ItemStack cursor = event.getOldCursor();
        for (CharmEffect effect : getCharmEffects(cursor)) {
            effect.markPlayerHeld(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMainHandDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) ||
                !event.getInventorySlots().contains(event.getWhoClicked().getInventory().getHeldItemSlot()))
            return;

        for (CharmEffect effect : getCharmEffects(event.getOldCursor())) {
            effect.markPlayerHeld(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        this.cantActivateAbilities.setCooldown(event.getPlayer().getUniqueId(), 100L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.useItemInHand() == Event.Result.ALLOW)
            return;

        if (this.cantActivateAbilities.isOnCooldown(event.getPlayer().getUniqueId()))
            return;

        if (event.getHand() == EquipmentSlot.HAND && !allowMainHandCharms)
            return;

        ItemStack clickedWithItem = event.getItem();
        for (CharmEffect effect : getCharmEffects(clickedWithItem)) {
            effect.onInteract(event.getPlayer());
            effect.tryStartAbility(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        for (CharmEffect effect : getHeldCharmEffects(event.getPlayer())) {
            effect.onPlayerInteract(event.getPlayer(), event.getRightClicked());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (CharmEffect effect : getHeldCharmEffects(event.getPlayer())) {
            effect.playerQuit(event.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (CharmEffect effect : getHeldCharmEffects(event.getPlayer())) {
            effect.markPlayerHeld(event.getPlayer());
        }
    }

    private void updateHeldCharms(Player player, ItemStack oldItem, ItemStack newItem) {
        List<CharmEffect> oldCharmEffects = getCharmEffects(oldItem);
        for (CharmEffect effect : oldCharmEffects) {
            effect.stopPlayerHold(player);
        }

        List<CharmEffect> newCharms = getCharmEffects(newItem);
        for (CharmEffect effect : newCharms) {
            effect.markPlayerHeld(player);
        }
    }

    private void updateHeldCharms(Player player, Set<CharmEffect> heldCharmEffects) {
        // Remove all old effects.
        for (Charm charm : this.charms.values()) {
            for (CharmEffect effect : charm.charmEffects()) {
                if (heldCharmEffects.contains(effect))
                    continue;

                effect.stopPlayerHold(player);
            }
        }

        for (CharmEffect effect : heldCharmEffects) {
            effect.markPlayerHeld(player);
        }
    }
}
