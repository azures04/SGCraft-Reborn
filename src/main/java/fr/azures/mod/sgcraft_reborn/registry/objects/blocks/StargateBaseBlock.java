package fr.azures.mod.sgcraft_reborn.registry.objects.blocks;

import java.util.List;

import fr.azures.mod.sgcraft_reborn.registry.ModTilesEntities;
import fr.azures.mod.sgcraft_reborn.registry.objects.structures.StargateStructure;
import fr.azures.mod.sgcraft_reborn.registry.objects.tiles.StargateBaseTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class StargateBaseBlock extends Block {

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	
	public StargateBaseBlock() {
        super(Properties.of(Material.STONE).harvestLevel(5).air());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}
	
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

	@Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }
    
    @Override
    public void onPlace(BlockState newState, World world, BlockPos pos, BlockState oldState, boolean p_220082_5_) {
    	Block[] blocks = getPatternStructure(world, pos);
		Minecraft.getInstance().player.sendMessage(new StringTextComponent("Is structure valid: " + StargateStructure.isStructureValid(blocks)), null);;
    	super.onPlace(newState, world, pos, oldState, p_220082_5_);
    }
    
    public Block[] getPatternStructure(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Direction facing = state.getValue(FACING);

        BlockPos leftPos1 = pos.relative(facing.getCounterClockWise());
        BlockPos leftPos2 = leftPos1.relative(facing.getCounterClockWise());
        BlockPos rightCornerTop = leftPos2.above(4);
        BlockPos leftCornerTop = rightCornerTop.relative(facing.getClockWise(), 4);
        
        Block[] blocks = new Block[15];
        blocks[0] = world.getBlockState(leftPos1).getBlock();
        blocks[1] = world.getBlockState(leftPos2).getBlock();
        blocks[2] = world.getBlockState(leftPos2.above(1)).getBlock();
        blocks[3] = world.getBlockState(leftPos2.above(2)).getBlock();
        blocks[4] = world.getBlockState(leftPos2.above(3)).getBlock();
        blocks[5] = world.getBlockState(leftPos2.above(4)).getBlock();
        blocks[6] = world.getBlockState(rightCornerTop.relative(facing.getClockWise())).getBlock();
        blocks[7] = world.getBlockState(rightCornerTop.relative(facing.getClockWise(), 2)).getBlock();
        blocks[8] = world.getBlockState(rightCornerTop.relative(facing.getClockWise(), 3)).getBlock();
        blocks[9] = world.getBlockState(leftCornerTop).getBlock();
        blocks[10] = world.getBlockState(leftCornerTop.below(1)).getBlock();
        blocks[11] = world.getBlockState(leftCornerTop.below(2)).getBlock();
        blocks[12] = world.getBlockState(leftCornerTop.below(3)).getBlock();
        blocks[13] = world.getBlockState(leftCornerTop.below(4)).getBlock();
        blocks[14] = world.getBlockState(leftCornerTop.below(4).relative(facing.getCounterClockWise())).getBlock();
        return blocks;
    }
    
    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModTilesEntities.STARGATE_BASE.get().create();
    }
    
    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.loot.LootContext.Builder builder) {
        World world = builder.getLevel();
        BlockPos pos = new BlockPos(builder.getParameter(LootParameters.ORIGIN));
        StargateBaseTile blockEntity = (StargateBaseTile) world.getBlockEntity(pos);
        if (blockEntity != null) {
        	
        }
    	return super.getDrops(state, builder);
    }
    
    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
    	if (world.isClientSide) {
    		TileEntity tile = world.getBlockEntity(pos);
			if (player.isCrouching()) {
				Minecraft.getInstance().player.sendMessage(new StringTextComponent("Sneaking"), null);
			}
    		if (tile instanceof StargateBaseTile) {
    			StargateBaseTile stargateBaseTile = (StargateBaseTile) tile;
    			ItemStack item = player.getItemInHand(hand);
				if (stargateBaseTile.canAcceptUpgrade(item)) {
					Minecraft.getInstance().player.sendMessage(new StringTextComponent("Upgraded"), null);
					Minecraft.getInstance().player.sendMessage(new StringTextComponent(String.valueOf(player.isCrouching())), null);
					stargateBaseTile.addUpgrade(item);
					if (!player.isCreative()) {
						item.shrink(1);
					}
                    return ActionResultType.SUCCESS;
				}
			}
		}
        return ActionResultType.PASS;
    }
}
