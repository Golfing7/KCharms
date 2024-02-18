package com.golfing8.kcharm.module.struct;

import com.golfing8.kcharm.module.CharmModule;
import com.golfing8.kcharm.module.effect.CharmEffect;
import com.golfing8.kcharm.module.effect.CharmEffectActive;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.google.common.base.Preconditions;
import de.tr7zw.kcommon.nbtapi.NBTCompound;
import de.tr7zw.kcommon.nbtapi.NBTItem;
import de.tr7zw.kcommon.nbtapi.NBTType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Contains structural data about a given charm.
 *
 * @param id           The internal IF of this charm
 * @param charmEffects The effects the player will receive when holding this charm.
 */
public record Charm(String id, List<CharmEffect> charmEffects, ItemStackBuilder charmItemFormat) {
    /**
     * Builds the charm item from the format.
     *
     * @return the built item.
     */
    public ItemStack buildItem() {
        ItemStack built = this.charmItemFormat.buildCached();
        NBTItem nbtItem = new NBTItem(built);
        NBTCompound compound = nbtItem.addCompound(CharmModule.CHARM_ITEM_KEY);
        compound.setBoolean(this.id, true);
        return nbtItem.getItem();
    }

    /**
     * Gets the display name of this charm.
     *
     * @return the display name.
     */
    public String getDisplayName() {
        if (charmItemFormat.getItemName() != null)
            return charmItemFormat.getItemName();

        return id;
    }

    /**
     * Checks if the player is holding the charm.
     *
     * @param player the player.
     * @return if they're holding the charm.
     */
    public boolean isHoldingCharm(Player player) {
        CharmModule module = CharmModule.get();
        ItemStack[] itemStacks = module.getCharmItems(player);
        for (ItemStack stack : itemStacks) {
            if (stack == null || stack.getType() != charmItemFormat.getItemType().parseMaterial() || !stack.hasItemMeta())
                continue;

            NBTItem nbtItem = new NBTItem(stack);
            if (!nbtItem.hasTag(CharmModule.CHARM_ITEM_KEY, NBTType.NBTTagCompound))
                continue;

            NBTCompound compound = nbtItem.getCompound(CharmModule.CHARM_ITEM_KEY);
            return compound.hasTag(id);
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
        Preconditions.checkArgument(section.isList("effects"), "Charm %s must contain effects".formatted(section.getName()));
        Preconditions.checkArgument(section.isConfigurationSection("item"), "Charm %s must contain an item".formatted(section.getName()));

        CharmModule module = CharmModule.get();
        List<CharmEffect> effects = new ArrayList<>();
        for (String effectKey : section.getStringList("effects")) {
            CharmEffect charmEffect = module.getCharmEffects().get(effectKey);
            if (charmEffect == null) {
                module.getPlugin().getLogger().warning("Effect with key %s doesn't exist!".formatted(effectKey));
                continue;
            }

            effects.add(charmEffect);
        }

        ItemStackBuilder builder = new ItemStackBuilder(section.getConfigurationSection("item"));
        return new Charm(section.getName(), effects, builder);
    }
}
