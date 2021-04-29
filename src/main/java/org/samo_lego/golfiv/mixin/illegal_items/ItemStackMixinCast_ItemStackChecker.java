package org.samo_lego.golfiv.mixin.illegal_items;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.golfiv.casts.ItemStackChecker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

/**
 * Additional methods for checking ItemStack's legality.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixinCast_ItemStackChecker implements ItemStackChecker {

    @Shadow private int count;

    @Shadow public abstract ListTag getEnchantments();

    @Shadow public abstract void removeSubTag(String key);

    @Shadow public abstract int getMaxCount();

    @Shadow public abstract void setCount(int count);

    @Shadow @Nullable public abstract CompoundTag getSubTag(String key);

    private final ItemStack itemStack = (ItemStack) (Object) this;

    /**
     * Sets the appropriate ItemStack size,
     * removes disallowed enchantments.
     *
     * @param survival w
     */
    @Override
    public void makeLegal(boolean survival) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.fromTag(this.getEnchantments());

        // Checks item enchantments
        for(Map.Entry<Enchantment, Integer> ench : enchantments.entrySet()) {
            Enchantment enchantment = ench.getKey();
            int level = ench.getValue();

            Set<Enchantment> otherEnchants = EnchantmentHelper.get(this.itemStack).keySet();
            otherEnchants.remove(enchantment);

            if(!enchantment.isAcceptableItem(this.itemStack) || !EnchantmentHelper.isCompatible(otherEnchants, enchantment) || level > enchantment.getMaxLevel()) {
                this.removeSubTag("Enchantments");
                break;
            }
        }

        // Checks potion
        if(this.itemStack.getItem() == Items.POTION || this.itemStack.getItem() == Items.SPLASH_POTION || this.itemStack.getItem() == Items.LINGERING_POTION) {
            List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(this.itemStack);
            for(StatusEffectInstance effect : effects) {
                if(effect.getAmplifier() > 1) {
                    this.removeSubTag("CustomPotionEffects");
                    this.removeSubTag("Potion");
                    break;
                }
            }
        }

        Identifier id = Registry.ITEM.getId(this.itemStack.getItem());
        if(survival && (golfConfig.items.survival.bannedItems.contains(id.toString()) || (golfConfig.items.survival.bannedItems.contains("minecraft:spawn_egg") && this.itemStack.getItem() instanceof SpawnEggItem))) {
            this.setCount(0);
        }

        if(this.count > this.getMaxCount()) {
            this.count = 1;
        }
    }
}
