package fr.azures.mod.sgcraft_reborn.registry.objects.blocks;

import fr.azures.mod.sgcraft_reborn.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class StargateBaseBlock extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    
    public StargateBaseBlock() {
        super(Properties.create(Material.ROCK));
    }
    
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.with(FACING, placer.getHorizontalFacing().getOpposite()), 2);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
    	if (isStructureFormed(worldIn, pos)) {
			
		}
    	return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }
    
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote) {
            boolean isActive = state.get(ACTIVE);
            if (isActive != worldIn.isBlockPowered(pos)) {
                worldIn.setBlockState(pos, state.with(ACTIVE, !isActive), 2);
            }
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!worldIn.isRemote) {
            boolean isActive = state.get(ACTIVE);
            if (isActive != worldIn.isBlockPowered(pos)) {
                worldIn.setBlockState(pos, state.with(ACTIVE, !isActive), 2);
            }
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    public Direction getFacing(BlockState state) {
        return state.get(FACING);
    }

    public boolean isActive(BlockState state) {
        return state.get(ACTIVE);
    }

    public BlockState withFacing(BlockState state, Direction facing) {
        return state.with(FACING, facing);
    }
    
    public boolean isStructureFormed(World world, BlockPos controllerPos) {
    	Direction facing = getFacing(world.getBlockState(controllerPos));
        Block[][] STRUCTURE_PATTERN = {
            {ModBlocks.STARGATE_CHEVRON.get(), ModBlocks.STARGATE_RING.get(), ModBlocks.STARGATE_BASE.get(), ModBlocks.STARGATE_RING.get(), ModBlocks.STARGATE_CHEVRON.get()},
            {ModBlocks.STARGATE_RING.get(), Blocks.AIR.getBlock(), Blocks.AIR.getBlock(), Blocks.AIR.getBlock(), ModBlocks.STARGATE_RING.get()},
            {ModBlocks.STARGATE_CHEVRON.get(), Blocks.AIR.getBlock(), Blocks.AIR.getBlock(), Blocks.AIR.getBlock(), ModBlocks.STARGATE_CHEVRON.get()},
            {ModBlocks.STARGATE_RING.get(), Blocks.AIR.getBlock(), Blocks.AIR.getBlock(), Blocks.AIR.getBlock(), ModBlocks.STARGATE_RING.get()},
            {ModBlocks.STARGATE_CHEVRON.get(), ModBlocks.STARGATE_RING.get(), ModBlocks.STARGATE_CHEVRON.get(), ModBlocks.STARGATE_RING.get(), ModBlocks.STARGATE_CHEVRON.get()}

        };
        for (int y = 0; y < 5; y++) {
			for (int x = 0; x < 5; x++) {
				BlockState actualBlockState = null;
				if (facing == Direction.WEST|| facing == Direction.EAST) {
					actualBlockState = world.getBlockState(new BlockPos(controllerPos.getX(), controllerPos.getY() + y, controllerPos.getZ() + (x - 2)));
				} else if (facing == Direction.NORTH|| facing == Direction.SOUTH) {
					actualBlockState = world.getBlockState(new BlockPos(controllerPos.getX() + (x - 2), controllerPos.getY() + y, controllerPos.getZ()));
				}
				if (actualBlockState != null && actualBlockState.getBlock().getRegistryName() != STRUCTURE_PATTERN[y][x].getRegistryName()) {
					world.setBlockState(new BlockPos(controllerPos.getX() + (x - 2), controllerPos.getY() + y, controllerPos.getZ()), Blocks.ACACIA_PLANKS.getDefaultState());
					return false;
				}
			}
		}
        return true;
    }
}
