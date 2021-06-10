package org.samo_lego.golfiv.casts;

import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.TippedArrowItem;
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

        if(original.getItem() instanceof PotionItem || original.getItem() instanceof TippedArrowItem) {
            fakedStack.getOrCreateTag().putInt("CustomPotionColor", PotionUtil.getColor(original));
        }

        return fakedStack;
    }
}
