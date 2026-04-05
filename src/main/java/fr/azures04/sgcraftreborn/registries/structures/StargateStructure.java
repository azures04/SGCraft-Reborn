package fr.azures04.sgcraftreborn.registries.structures;

import fr.azures04.sgcraftreborn.registries.blocks.StargateBaseBlock;
import fr.azures04.sgcraftreborn.registries.blocks.StargateChevronBlock;
import fr.azures04.sgcraftreborn.registries.blocks.StargateRingBlock;
import fr.azures04.sgcraftreborn.registries.tiles.StargateBaseTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateStructure {

    public static final int[][] CHEVRON_POSITIONS = {
        {0, 4}, {2, 4}, {-2, 4},
        {2, 2}, {-2, 2},
        {2, 0}, {-2, 0}
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

    public static boolean checkStructure(World world, BlockPos base, EnumFacing facing) {
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

    public static void hideStructure(World world, BlockPos base, EnumFacing facing) {
        if (world.getBlockState(base).getBlock() instanceof StargateBaseBlock) {
            world.setBlockState(base, world.getBlockState(base).with(StargateBaseBlock.INVISIBLE, true), 3);
        }

        for (int[] pos : CHEVRON_POSITIONS) {
            BlockPos worldPos = getWorldPos(base, facing, pos[0], pos[1]);
            if (world.getBlockState(worldPos).getBlock() instanceof StargateChevronBlock) {
                world.setBlockState(worldPos, world.getBlockState(worldPos).with(StargateChevronBlock.INVISIBLE, true), 3);
            }
        }
        for (int[] pos : RING_POSITIONS) {
            BlockPos worldPos = getWorldPos(base, facing, pos[0], pos[1]);
            if (world.getBlockState(worldPos).getBlock() instanceof StargateRingBlock) {
                world.setBlockState(worldPos, world.getBlockState(worldPos).with(StargateRingBlock.INVISIBLE, true), 3);
            }
        }
    }

    public static void showStructure(World world, BlockPos base, EnumFacing facing) {
        if (world.getBlockState(base).getBlock() instanceof StargateBaseBlock) {
            world.setBlockState(base, world.getBlockState(base).with(StargateBaseBlock.INVISIBLE, false), 3);
        }
        for (int[] pos : CHEVRON_POSITIONS) {
            BlockPos worldPos = getWorldPos(base, facing, pos[0], pos[1]);
            if (world.getBlockState(worldPos).getBlock() instanceof StargateChevronBlock) {
                world.setBlockState(worldPos, world.getBlockState(worldPos).with(StargateChevronBlock.INVISIBLE, false), 3);
            }
        }
        for (int[] pos : RING_POSITIONS) {
            BlockPos worldPos = getWorldPos(base, facing, pos[0], pos[1]);
            if (world.getBlockState(worldPos).getBlock() instanceof StargateRingBlock) {
                world.setBlockState(worldPos, world.getBlockState(worldPos).with(StargateRingBlock.INVISIBLE, false), 3);
            }
        }
    }

    public static void notifyNearbyBases(World world, BlockPos pos) {
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos checkPos = pos.add(x, y, z);
                    IBlockState checkState = world.getBlockState(checkPos);
                    if (checkState.getBlock() instanceof StargateBaseBlock) {
                        EnumFacing facing = checkState.get(StargateBaseBlock.FACING);
                        boolean formed = checkStructure(world, checkPos, facing);
                        TileEntity te = world.getTileEntity(checkPos);
                        if (te instanceof StargateBaseTileEntity) {
                            ((StargateBaseTileEntity) te).setMerged(formed);
                        }
                    }
                }
            }
        }
    }
}
