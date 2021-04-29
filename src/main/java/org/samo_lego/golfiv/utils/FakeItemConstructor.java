package org.samo_lego.golfiv.utils;

import net.minecraft.item.ItemStack;

/**
 * Creates a fake ItemStack based on the
 * provided stack.
 *
 * E.g. removes enchantment info, stack size, etc.
 * Used on ground ItemEntities / opponent's stacks etc.
 */
public class FakeItemConstructor {

    public static ItemStack fakeStack(ItemStack original, int count) {
        ItemStack fakedStack = new ItemStack(original.getItem(), count);

        if(original.hasEnchantments())
            fakedStack.addEnchantment(null, 0);

        return fakedStack;
    }
}
