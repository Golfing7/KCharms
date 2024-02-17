package com.golfing8.kcharm.module.struct;

import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcharm.module.effect.CharmEffect;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.google.common.base.Preconditions;
import de.tr7zw.kcommon.nbtapi.NBTItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains structural data about a given charm.
 *
 * @param id           The internal IF of this charm
 * @param charmEffects The effects the player will receive when holding this charm.
 */
public record Charm(String id, List<CharmEffect> charmEffects, ItemStackBuilder charmItemFormat) {
    private static final String CHARM_ITEM_KEY = "kcharm-type";

    /**
     * Checks if the player is holding the charm.
     *
     * @param player the player.
     * @return if they're holding the charm.
     */
    public boolean isHoldingCharm(Player player) {
        CharmModule module = CharmModule.get();
        ItemStack[] itemStacks = module.getPotentialCharmSlots(player);
        for (ItemStack stack : itemStacks) {
            if (stack == null || stack.getType() != charmItemFormat.getItemType().parseMaterial() || !stack.hasItemMeta())
                continue;

            NBTItem nbtItem = new NBTItem(stack);
            if (!nbtItem.hasTag(CHARM_ITEM_KEY))
                continue;

            return nbtItem.getStringList(CHARM_ITEM_KEY).contains(id);
        }
        return false;
    }

    /**
     * Loads a charm from its config definition.
     *
     * @param section the config defintion.
     * @return the section.
     */
    public static Charm fromConfig(ConfigurationSection section) {
        Preconditions.checkArgument(section.isConfigurationSection("effects"), "Charm %s must contain effects".formatted(section.getName()));
        Preconditions.checkArgument(section.isConfigurationSection("item"), "Charm %s must contain an item".formatted(section.getName()));

        List<CharmEffect> effects = new ArrayList<>();
        ConfigurationSection effectSection = section.getConfigurationSection("effects");
        for (String effectKey : effectSection.getKeys(false)) {
            //TODO
        }

        ItemStackBuilder builder = new ItemStackBuilder(section.getConfigurationSection("item"));
        return new Charm(section.getName(), effects, builder);
    }
}
