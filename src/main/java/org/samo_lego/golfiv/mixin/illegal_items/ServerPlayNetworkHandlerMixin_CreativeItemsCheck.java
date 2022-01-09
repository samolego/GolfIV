package org.samo_lego.golfiv.mixin.illegal_items;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.samo_lego.golfiv.casts.ItemStackChecker;
import org.samo_lego.golfiv.storage.GolfConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin_CreativeItemsCheck {
    @Unique
    private final Int2ObjectMap<NbtCompound> nbtMap = new Int2ObjectOpenHashMap<>();
    private boolean nbtMapPopulated = false;

    @Shadow
    public ServerPlayerEntity player;

    /**
     * Recovers stack NBT and clears illegal tags from creative items while still allowing pick block function.
     * <p>
     * If {@link GolfConfig.Packet#patchItemKickExploit Patch Item Kick Exploit} is enabled and the tag {@code GolfIV}
     * is present, the hash is looked up in {@link #nbtMap} to recover the original NBT of the item, and allowing
     * bypass of {@link GolfConfig.IllegalItems.Creative#removeCreativeNBTTags Remove Creative NBT Tags}.
     * <p>
     * If {@link GolfConfig.IllegalItems.Creative#removeCreativeNBTTags Remove Creative NBT Tags} is enabled,
     * and the tag {@code GolfIV} was not found or invalid, then the items are sanitised in accordance to the
     * set {@link GolfConfig.IllegalItems.Creative#whitelistedNBT Whitelisted NBT} in the config.
     *
     * @param itemStack The stack to either recover the NBT by hash from, or to be sanitized.
     * @return Recovered stack if GolfIV hash tag is present and valid, "sanitized" stack otherwise.
     * @author samo_lego
     * @author KJP12
     * @see ItemStackChecker#fakeStack(ItemStack, boolean)
     * @see org.samo_lego.golfiv.event.S2CPacket.ItemInventoryKickPatch
     * @see GolfConfig.Packet#patchItemKickExploit
     * @see GolfConfig.IllegalItems.Creative#removeCreativeNBTTags
     * @see GolfConfig.IllegalItems.Creative#whitelistedNBT
     */
    @ModifyVariable(
            method = "onCreativeInventoryAction(Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/BlockItem;getBlockEntityNbt(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/nbt/NbtCompound;"
            )
    )
    private ItemStack checkCreativeItem(ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            NbtCompound compoundTag = itemStack.getNbt();
            checkTag:
            if (compoundTag != null && !compoundTag.isEmpty()) {
                // In the case of the GolfIV tag, the NBT will be determined by the hash.
                // Since the hash usually means it already exists in the inventory, we can
                // assume that it's either been given by OP, or already existed before.
                // We will check to see if patchItemKickExploit is enabled before continuing however.
                if (golfConfig.packet.patchItemKickExploit && compoundTag.contains("GolfIV", NbtElement.INT_TYPE)) {
                    int mapHash = compoundTag.getInt("GolfIV");
                    if (!nbtMapPopulated && !nbtMap.containsKey(mapHash)) {
                        // If this gets called, it means that the player cloned it via middle-click.
                        // Instead of falling back to the sanitiser, we should make sure it isn't
                        // already in the inventory as something else.
                        populateNbtMap();
                    }
                    NbtCompound newNbt = nbtMap.get(mapHash);
                    if (newNbt != null) {
                        itemStack.setNbt(newNbt.copy());
                        break checkTag;
                    }
                    compoundTag.remove("GolfIV");
                }
                if (golfConfig.items.creative.removeCreativeNBTTags) {
                    NbtCompound newData = new NbtCompound();
                    golfConfig.items.creative.whitelistedNBT.forEach(tag -> {
                        if (compoundTag.contains(tag)) {
                            newData.put(tag, compoundTag.get(tag));
                        }
                    });
                    itemStack.setNbt(newData);
                    //noinspection ConstantConditions
                    ((ItemStackChecker) (Object) itemStack).makeLegal(false);
                }
            }
        }
        return itemStack;
    }

    /**
     * Slowly populates {@link #nbtMap} to have a copy of good NBT for creative players.
     * <p>
     * If it's the same item being set, the hash is compared if it's present.
     * If the hash matches the NBT, the NBT is stored and the count is set.
     * <p>
     * If it's not the same item, the hash of the old NBT is computed and
     * stored for in case the creative player picked up or swapped the item.
     * The new stack then is used to look up the NBT if the hash is set, and
     * overwrites the NBT received from the creative player if present.
     *
     * @param self     The slot being mutated.
     * @param newStack The item stack replacing the currently set stack.
     * @implNote NBT is only checked and stored when {@link GolfConfig.Packet#patchItemKickExploit
     * Patch Item Kick Exploit} is enabled.
     * @author KJP12
     * @see GolfConfig.Packet#patchItemKickExploit
     */
    @Redirect(
            method = "onCreativeInventoryAction",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;setStack(Lnet/minecraft/item/ItemStack;)V")
    )
    private void onCreativeSetStack(Slot self, ItemStack newStack) {
        // Check if patchItemKickExploit is enabled before continuing.
        // As this can be costly, we should have an inexpensive check
        // before the actual logic.
        if (golfConfig.packet.patchItemKickExploit) {
            ItemStack oldStack = self.getStack();

            // If it's getting replaced, we cannot exactly verify if it's because the client
            // decided to delete it or if it picked it up; the creative inventory works quite
            // differently from every other inventory in that regard.
            // This does allow for passive use before falling back to the more intensive method
            // of populating the map manually.
            if (!oldStack.isEmpty()) {
                NbtCompound oldNbt = oldStack.getNbt();
                if (oldNbt != null) {
                    int hash = oldNbt.hashCode();
                    NbtCompound mapNbt = nbtMap.get(hash);
                    // TODO: This may ideally be a check to see if the hash is getting overwritten.
                    //  It does seem unlikely this should actually happen normally tho.
                    if (!oldNbt.equals(mapNbt)) {
                        nbtMap.put(hash, oldNbt);
                    }
                }
            }
        }

        self.setStack(newStack);
    }

    /**
     * Clears {@link #nbtMap} when the current screen is closed.
     * <p>
     * This allows for the map to be reused, and prevents indefinite
     * leaking for the session of the player.
     * <p>
     * The injection point is purposefully after {@link net.minecraft.network.NetworkThreadUtils#forceMainThread(Packet, PacketListener, ServerWorld)}
     * to prevent asynchronous access of the map.
     *
     * @param packet Ignored by the method. Normally is to check sync ID.
     * @param ci     Ignored by the method. Normally is to cancel the method or check its name.
     * @implNote Despite being only relevant for {@link GolfConfig.Packet#patchItemKickExploit
     * Patch Item Kick Exploit}, this method will always attempt to clear the map.
     * @author KJP12
     * @see GolfConfig.Packet#patchItemKickExploit
     */
    @Inject(method = "onCloseHandledScreen", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
            shift = At.Shift.AFTER))
    private void onCloseScreen(CloseHandledScreenC2SPacket packet, CallbackInfo ci) {
        // Allows reuse and prevents indefinite leaking.
        nbtMap.clear();
        nbtMapPopulated = false;
    }

    /**
     * Populates {@link #nbtMap} in the event that the client sends an
     * unknown hash, usually by cloning via middle-click.
     *
     * @author KJP12
     * @see #checkCreativeItem(ItemStack)
     */
    @Unique
    private void populateNbtMap() {
        PlayerInventory inventory = player.getInventory();
        inventory.main.forEach(this::putNbt);
        inventory.armor.forEach(this::putNbt);
        inventory.offHand.forEach(this::putNbt);
        nbtMapPopulated = true;
    }

    /**
     * Method reference for mapping NBT to hashcode if not empty.
     *
     * @param stack The stack to copy and store the NBT of.
     * @author KJP12
     */
    @Unique
    private void putNbt(ItemStack stack) {
        NbtCompound compound = stack.getNbt();
        if (compound == null || compound.isEmpty()) return;
        nbtMap.put(compound.hashCode(), compound.copy());
    }
}