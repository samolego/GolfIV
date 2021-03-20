package org.samo_lego.golfiv.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.samo_lego.golfiv.casts.Golfer;

import static org.samo_lego.golfiv.GolfIV.golfConfig;

/**
 * Handles player trying to break the block while having inventory open.
 */
public class IllegalBlockBreakCheck implements PlayerBlockBreakEvents.Before {
    public IllegalBlockBreakCheck() {
    }

    @Override
    public boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        return !golfConfig.main.checkIllegalActions || !((Golfer) player).hasOpenGui();
    }
}
