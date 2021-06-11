package org.samo_lego.golfiv.casts;

import net.minecraft.item.*;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;

/**
 * Checks iItemStacks.
 */
public interface ItemStackChecker {
    /**
     * Checks the ItemStack and makes it legal.
     * Removes all enchantments if they are incompatible or
     * have too high level.
     */
    void makeLegal(boolean survival);

    /**
     * Creates a fake ItemStack based on the
     * provided stack.
     *
     * E.g. removes enchantment info, stack size, etc.
     * Used on ground ItemEntities / opponent's stacks etc.
     *
     * @param original the original ItemStack
     * @param spoofCount whether or not the item count should be faked
     * @return the faked ItemStack
     */
    static ItemStack fakeStack(ItemStack original, boolean spoofCount) {
        ItemStack fakedStack = spoofCount ?
                new ItemStack(original.getItem(), original.getMaxCount()) :
                new ItemStack(original.getItem(), original.getCount());

        if(original.hasEnchantments())
            fakedStack.addEnchantment(null, 0);

        Item item = original.getItem();
        if(item instanceof PotionItem || item instanceof TippedArrowItem) {
            // Lets dropping potions and arrows to be less of a 'surprising' change.
            fakedStack.getOrCreateTag().putInt("CustomPotionColor", PotionUtil.getColor(original));
        }

        if(item instanceof WritableBookItem || item instanceof WrittenBookItem) {
            // Prevents issues with other mods expecting pages to be present.
            fakedStack.putSubTag("pages", new NbtList());
        }

        return fakedStack;
    }
}
