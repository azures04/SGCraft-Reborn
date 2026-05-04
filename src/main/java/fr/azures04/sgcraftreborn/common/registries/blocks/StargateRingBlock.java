package fr.azures04.sgcraftreborn.common.registries.blocks;

import fr.azures04.sgcraftreborn.common.registries.structures.StargateStructure;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
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
    public boolean isNormalCube(BlockState p_220081_1_, IBlockReader p_220081_2_, BlockPos p_220081_3_) {
        return false;
    }

    @Override
    public boolean causesSuffocation(BlockState p_220060_1_, IBlockReader p_220060_2_, BlockPos p_220060_3_) {
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(INVISIBLE, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState()
            .with(INVISIBLE, false)
            .with(WATERLOGGED, false);
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!worldIn.isRemote) {
            StargateStructure.notifyNearbyBases(worldIn, pos);
        }
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isRemote) {
                StargateStructure.notifyNearbyBases(worldIn, pos);
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public int getOpacity(BlockState state, IBlockReader reader, BlockPos pos) {
        return 0;
    }

    @Override
    public Fluid pickupFluid(IWorld worldIn, BlockPos pos, BlockState state) {
        if (state.get(WATERLOGGED)) {
            worldIn.setBlockState(pos, state.with(WATERLOGGED, false), 3);
            return Fluids.WATER;
        }
        return Fluids.EMPTY;
    }

    @Override
    public boolean canContainFluid(IBlockReader reader, BlockPos pos, BlockState state, Fluid fluidIn) {
        return state.get(INVISIBLE) && fluidIn == Fluids.WATER;
    }

    @Override
    public boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, IFluidState fluidStateIn) {
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
}