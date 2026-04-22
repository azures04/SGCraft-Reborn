package fr.azures04.sgcraftreborn.common.registries.blocks;

import fr.azures04.sgcraftreborn.common.registries.structures.StargateStructure;
import net.minecraft.block.Block;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class StargateRingBlock extends Block implements ILiquidContainer, IBucketPickupHandler {

    public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public StargateRingBlock(Properties properties) {
        super(properties);
        setDefaultState(this.stateContainer.getBaseState()
            .with(INVISIBLE, false)
            .with(WATERLOGGED, false)
        );
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockReader world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(INVISIBLE, WATERLOGGED);
    }

    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        return (IBlockState) this.getDefaultState()
            .with(INVISIBLE, false)
            .with(WATERLOGGED, false);
    }

    @Override
    public void onBlockAdded(IBlockState state, World worldIn, BlockPos pos, IBlockState oldState) {
        if (!worldIn.isRemote) {
            StargateStructure.notifyNearbyBases(worldIn, pos);
        }
        super.onBlockAdded(state, worldIn, pos, oldState);
    }

    @Override
    public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isRemote) {
                StargateStructure.notifyNearbyBases(worldIn, pos);
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public int getOpacity(IBlockState state, IBlockReader worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean propagatesSkylightDown(IBlockState state, net.minecraft.world.IBlockReader reader, net.minecraft.util.math.BlockPos pos) {
        return true;
    }

    @Override
    public IFluidState getFluidState(IBlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, IBlockState state, Fluid fluidIn) {
        return state.get(INVISIBLE) && fluidIn == Fluids.WATER;
    }

    @Override
    public boolean receiveFluid(IWorld worldIn, BlockPos pos, IBlockState state, IFluidState fluidStateIn) {
        if (!state.get(INVISIBLE)) {
            return false;
        }

        if (!state.get(WATERLOGGED) && fluidStateIn.getFluid() == Fluids.WATER) {
            if (!worldIn.isRemote()) {
                worldIn.setBlockState(pos, state.with(WATERLOGGED, true), 3);
                worldIn.getPendingFluidTicks().scheduleTick(pos, fluidStateIn.getFluid(), fluidStateIn.getFluid().getTickRate(worldIn));
            }
            return true;
        }
        return false;
    }

    @Override
    public Fluid pickupFluid(IWorld worldIn, BlockPos pos, IBlockState state) {
        if (state.get(WATERLOGGED)) {
            worldIn.setBlockState(pos, state.with(WATERLOGGED, false), 3);
            return Fluids.WATER;
        }
        return Fluids.EMPTY;
    }
}
