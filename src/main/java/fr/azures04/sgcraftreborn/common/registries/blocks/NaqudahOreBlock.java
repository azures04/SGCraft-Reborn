package fr.azures04.sgcraftreborn.common.registries.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class NaqudahOreBlock extends Block {

    public NaqudahOreBlock(Block.Properties properties) {
        super(properties);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isNormalCube(BlockState state, IBlockReader world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean causesSuffocation(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

}
