package fr.azures04.sgcraftreborn.common.registries.blocks;

import fr.azures04.sgcraftreborn.client.models.ISpecialItemRenderer;
import fr.azures04.sgcraftreborn.common.registries.blocks.states.StargateControllerStatus;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateControllerTileEntity;
import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;
import net.minecraft.block.*;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;

public class StargateControllerBlock extends Block implements ISpecialItemRenderer, ILiquidContainer, IBucketPickupHandler {

    public static final DirectionProperty FACING  = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty <StargateControllerStatus> STATUS = EnumProperty.create("status", StargateControllerStatus.class);;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public StargateControllerBlock(Block.Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState()
            .with(FACING, Direction.NORTH)
            .with(STATUS, StargateControllerStatus.UNLINKED)
            .with(WATERLOGGED, false)
        );
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATUS, WATERLOGGED);
    }

    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean isNormalCube(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    @Nullable
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new StargateControllerTileEntity();
    }

    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState()
            .with(FACING, context.getPlacementHorizontalFacing())
            .with(STATUS, StargateControllerStatus.UNLINKED)
            .with(WATERLOGGED, false);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        Direction side = rayTraceResult.getFace();
        if (worldIn.isRemote && side == Direction.UP) {

            StargateControllerTileEntity controller = (StargateControllerTileEntity) worldIn.getTileEntity(pos);
            if (controller == null || !controller.isLinked()) return true;

            ExtendedPos linkedPos = controller.getLinkedStargate();
            if (linkedPos == null) return true;

            TileEntity baseTe = worldIn.getTileEntity(linkedPos);
            boolean hasChevron = (baseTe instanceof StargateBaseTileEntity) && ((StargateBaseTileEntity) baseTe).hasChevronUpgrade();

            DistExecutor.runWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
                ExtendedPos exPos = new ExtendedPos(pos, worldIn.dimension.getType().getId());
                StargateControllerStatus status = state.get(STATUS);
                fr.azures04.sgcraftreborn.client.registries.ModScreens.open("controller_main", exPos, status, hasChevron);
            });
            return true;
        } else {
            if (!worldIn.isRemote) {
                Direction blockFacing = state.get(FACING);
                if (side == blockFacing.getOpposite()) {
                    TileEntity controller = worldIn.getTileEntity(pos);
                    if (controller instanceof StargateControllerTileEntity) {
                        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) controller, pos);
                        return true;
                    }
                }
            }
            return true;
        }
    }

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        if (!world.isRemote) {
            StargateControllerTileEntity controller = (StargateControllerTileEntity) world.getTileEntity(pos);
            controller.getLinkedStargateTE(world);
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isRemote) {
                TileEntity te = worldIn.getTileEntity(pos);
                if (te instanceof StargateControllerTileEntity) {
                    StargateControllerTileEntity controller = (StargateControllerTileEntity) te;
                    controller.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent((inventory) -> {
                        for (int i = 0; i < inventory.getSlots(); ++i) {
                            ItemStack stack = inventory.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                InventoryHelper.spawnItemStack(worldIn, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ(), stack);
                            }
                        }
                    });
                }
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
        return fluidIn == Fluids.WATER;
    }

    @Override
    public boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, IFluidState fluidStateIn) {
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
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public Callable<ItemStackTileEntityRenderer> getISTER() {
        return fr.azures04.sgcraftreborn.client.models.tiles.items.StargateControllerISTER::new;
    }

    @Override
    public boolean isSolid(BlockState p_200124_1_) {
        return false;
    }
}