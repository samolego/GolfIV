package org.samo_lego.golfiv.casts;

import net.minecraft.item.ItemStack;

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
     */
    static ItemStack fakeStack(ItemStack original, int count) {
        ItemStack fakedStack = new ItemStack(original.getItem(), count);

        if(original.hasEnchantments())
            fakedStack.addEnchantment(null, 0);

        return fakedStack;
    }
}
