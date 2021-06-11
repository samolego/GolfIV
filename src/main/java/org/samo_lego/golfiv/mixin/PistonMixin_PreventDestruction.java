package org.samo_lego.golfiv.mixin;// Created 2021-08-06T02:20:50

import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

/**
 * Prevents a headless piston from breaking blocks on block updates.
 *
 * @author KJP12
 **/
@Mixin(PistonBlock.class)
public class PistonMixin_PreventDestruction {
    @Redirect(method = "onSyncedBlockEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private boolean redirectWorld(World world, BlockPos pos, boolean move) {
        if(!golfConfig.main.preventDestructionByHeadlessPistons || world.getBlockState(pos).isOf(Blocks.PISTON_HEAD)) {
            return world.removeBlock(pos, move);
        } else return false;
    }
}
