package fr.azures.mod.sgcraft_reborn.test;

import fr.azures.mod.sgcraft_reborn.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StargateStructure {
    private static final BlockState CHEVRON = ModBlocks.STARGATE_CHEVRON.get().getDefaultState();
    private static final BlockState RING = ModBlocks.STARGATE_RING.get().getDefaultState();
    private static final BlockState BASE = ModBlocks.STARGATE_BASE.get().getDefaultState();

    public static boolean isValidStructure(World world, BlockPos startPos) {
        // Define the structure composition
    	BlockState[][] structure = {
            { CHEVRON, Blocks.AIR.getDefaultState(), CHEVRON, Blocks.AIR.getDefaultState(), CHEVRON },
            { RING, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), RING },
            { CHEVRON, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), CHEVRON },
            { RING, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), RING },
            { CHEVRON, BASE, RING, BASE, CHEVRON }
        };

        // Check if the blocks match the structure
        for (int i = 0; i < structure.length; i++) {
            for (int j = 0; j < structure[i].length; j++) {
                BlockState expectedBlock = structure[i][j];
                BlockPos currentPos = startPos.add(i, 0, j);
                BlockState currentBlock = world.getBlockState(currentPos);

                if (currentBlock.getBlock() != expectedBlock.getBlock()) {
                    return false;
                }
            }
        }

        return true;
    }
}
