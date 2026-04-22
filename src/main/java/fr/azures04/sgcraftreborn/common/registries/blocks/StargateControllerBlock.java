package fr.azures04.sgcraftreborn.common.registries.blocks;

import fr.azures04.sgcraftreborn.client.models.ISpecialItemRenderer;
import fr.azures04.sgcraftreborn.client.models.tiles.items.StargateControllerISTER;
import fr.azures04.sgcraftreborn.client.screens.StargateControllerScreen;
import fr.azures04.sgcraftreborn.common.registries.blocks.states.StargateControllerStatus;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateBaseTileEntity;
import fr.azures04.sgcraftreborn.common.registries.tiles.StargateControllerTileEntity;
import fr.azures04.sgcraftreborn.common.util.math.ExtendedPos;
import net.minecraft.block.Block;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public class StargateControllerBlock extends Block implements ISpecialItemRenderer, ILiquidContainer, IBucketPickupHandler {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<StargateControllerStatus> STATUS = EnumProperty.create("status", StargateControllerStatus.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public StargateControllerBlock(Properties properties) {
        super(properties);
        setDefaultState(this.stateContainer.getBaseState()
            .with(FACING, EnumFacing.NORTH)
            .with(STATUS, StargateControllerStatus.UNLINKED)
            .with(WATERLOGGED, false)
        );
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(IBlockState state, IBlockReader world) {
        return new StargateControllerTileEntity();
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
        builder.add(FACING, STATUS, WATERLOGGED);
    }

    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        return (IBlockState) this.getDefaultState()
            .with(FACING, context.getPlacementHorizontalFacing())
            .with(STATUS, StargateControllerStatus.UNLINKED)
            .with(WATERLOGGED, false);
    }

    @Override
    public Callable<TileEntityItemStackRenderer> getISTER() {
        return StargateControllerISTER::new;
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            if (side == EnumFacing.UP) {
                ExtendedPos controllerPos = new ExtendedPos(pos, worldIn.dimension.getType().getId());
                StargateControllerTileEntity controller = (StargateControllerTileEntity) worldIn.getTileEntity(pos);
                StargateBaseTileEntity base = (StargateBaseTileEntity) worldIn.getTileEntity(controller.getLinkedStargate());
                Minecraft.getInstance().displayGuiScreen(new StargateControllerScreen(controllerPos, state.get(STATUS), base.hasChevronUpgrade()));
                return true;
            }
        }

        if (!worldIn.isRemote) {
            EnumFacing blockFacing = state.get(FACING);
            if (side == blockFacing.getOpposite()) {
                TileEntity te = worldIn.getTileEntity(pos);
                if (te instanceof StargateControllerTileEntity) {
                    NetworkHooks.openGui((EntityPlayerMP) player, (IInteractionObject) te, pos);
                    return true;
                }
            }
        }

        return true;
    }

    @Override
    public void onBlockAdded(IBlockState state, World worldIn, BlockPos pos, IBlockState oldState) {
        if (worldIn.isRemote) return;
        StargateControllerTileEntity controller = (StargateControllerTileEntity) worldIn.getTileEntity(pos);
        controller.getLinkedStargateTE(worldIn);
    }

    @Override
    public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
        if (worldIn.isRemote) return;
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof StargateControllerTileEntity) {
                StargateControllerTileEntity controller = (StargateControllerTileEntity) te;
                controller.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(inventory -> {
                    for (int i = 0; i < inventory.getSlots(); i++) {
                        ItemStack stack = inventory.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
                        }
                    }
                });
            }
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
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
        return true;
    }

    @Override
    public boolean receiveFluid(IWorld worldIn, BlockPos pos, IBlockState state, IFluidState fluidStateIn) {
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
