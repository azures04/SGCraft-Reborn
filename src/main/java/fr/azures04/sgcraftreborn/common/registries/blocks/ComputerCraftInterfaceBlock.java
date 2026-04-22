package fr.azures04.sgcraftreborn.common.registries.blocks;

import fr.azures04.sgcraftreborn.common.registries.tiles.ComputerCraftInterfaceTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class ComputerCraftInterfaceBlock extends Block  {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public ComputerCraftInterfaceBlock(Properties properties) {
        super(properties);
        setDefaultState(this.stateContainer.getBaseState().with(FACING, EnumFacing.NORTH));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        return (IBlockState) this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(IBlockState state, IBlockReader world) {
        return new ComputerCraftInterfaceTileEntity();
    }
}
