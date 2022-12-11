package org.samo_lego.golfiv.mixin.illegal_items;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
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

    @Shadow
    private int count;

    @Shadow
    public abstract NbtList getEnchantments();

    @Shadow
    public abstract int getMaxCount();

    @Shadow
    public abstract void setCount(int count);

    @Shadow
    public abstract void removeSubNbt(String key);

    @Shadow
    public abstract Item getItem();

    @Shadow
    private @Nullable NbtCompound nbt;
    private final ItemStack itemStack = (ItemStack) (Object) this;

    /**
     * Sets the appropriate ItemStack size,
     * removes disallowed enchantments.
     *
     * @param survival w
     */
    @Override
    public void makeLegal(boolean survival) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.fromNbt(this.getEnchantments());

        // Checks item enchantments
        if ((survival && golfConfig.items.survival.checkEnchants) || (!survival && golfConfig.items.creative.checkEnchants))
            for (Map.Entry<Enchantment, Integer> ench : enchantments.entrySet()) {
                Enchantment enchantment = ench.getKey();
                int level = ench.getValue();

                Set<Enchantment> otherEnchants = EnchantmentHelper.get(this.itemStack).keySet();
                otherEnchants.remove(enchantment);

                if (!enchantment.isAcceptableItem(this.itemStack) ||
                        !EnchantmentHelper.isCompatible(otherEnchants, enchantment) ||
                        level > enchantment.getMaxLevel()) {
                    this.removeSubNbt("Enchantments");
                    break;
                }
            }

        // Checks potion
        if (((survival && golfConfig.items.survival.checkPotionLevels) || (!survival && golfConfig.items.creative.checkPotionLevels)) &&
                (this.itemStack.getItem() == Items.POTION ||
                        this.itemStack.getItem() == Items.SPLASH_POTION ||
                        this.itemStack.getItem() == Items.LINGERING_POTION)) {
            List<StatusEffectInstance> effects = PotionUtil.getCustomPotionEffects(this.itemStack);

            for (StatusEffectInstance effect : effects) {
                if (effect.getAmplifier() > 1) {
                    this.removeSubNbt("CustomPotionEffects");
                    this.removeSubNbt("Potion");
                    break;
                }
            }
        }

        String nbt = golfConfig.items.survival.bannedItems.get(this.itemStack.getItem());
        NbtCompound tag = this.itemStack.getNbt();

        if (survival && nbt != null && (nbt.isEmpty() || tag != null && tag.toString().equals(nbt)) ||
                (golfConfig.items.survival.banSpawnEggs && this.itemStack.getItem() instanceof SpawnEggItem)) {
            this.setCount(0);
        } else if (((survival && golfConfig.items.survival.checkItemCount) ||
                (!survival && golfConfig.items.creative.checkItemCount)) &&
                this.count > this.getMaxCount()) {

            this.count = this.getMaxCount();
        }
    }
}
