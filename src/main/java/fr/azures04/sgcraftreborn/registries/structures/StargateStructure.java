package fr.azures04.sgcraftreborn.registries.structures;

import fr.azures04.sgcraftreborn.registries.blocks.StargateChevronBlock;
import fr.azures04.sgcraftreborn.registries.blocks.StargateRingBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class StargateStructure {

    public static final int[][] CHEVRON_POSITIONS = {
            {0, 4}, {2, 4}, {-2, 4},
            {2, 2}, {-2, 2}
    };

    public static final int[][] RING_POSITIONS = {
            {1, 4}, {-1, 4},
            {2, 3}, {-2, 3},
            {2, 1}, {-2, 1},
            {1, 0}, {-1, 0}
    };

    public static BlockPos getWorldPos(BlockPos base, EnumFacing facing, int gridX, int gridY) {
        switch (facing) {
            case NORTH:
                return base.add(-gridX, gridY, 0);
            case SOUTH:
                return base.add(gridX, gridY, 0);
            case EAST:
                return base.add(0, gridY, -gridX);
            case WEST:
                return base.add(0, gridY, gridX);
            case UP:
                return base.add(gridX, 0, -gridY);
            case DOWN:
                return base.add(gridX, 0, gridY);
            default:
                return base;
        }
    }

    public static boolean checkStructure(IBlockReader world, BlockPos base, EnumFacing facing) {
        for (int[] pos : CHEVRON_POSITIONS) {
            BlockPos worldPos = getWorldPos(base, facing, pos[0], pos[1]);
            if (!(world.getBlockState(worldPos).getBlock() instanceof StargateChevronBlock)) {
                return false;
            }
        }

        for (int[] pos : RING_POSITIONS) {
            BlockPos worldPos = getWorldPos(base, facing, pos[0], pos[1]);
            if (!(world.getBlockState(worldPos).getBlock() instanceof StargateRingBlock)) {
                return false;
            }
        }

        return true;
    }
}
